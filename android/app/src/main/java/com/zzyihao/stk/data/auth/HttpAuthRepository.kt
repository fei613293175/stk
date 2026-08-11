package com.zzyihao.stk.data.auth

import com.zzyihao.stk.data.session.TokenStore
import com.zzyihao.stk.data.session.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class HttpAuthRepository(
    private val endpoint: String,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun getBootstrap(): AuthBootstrapConfig = withContext(Dispatchers.IO) {
        val separator = if (endpoint.contains('?')) "&" else "?"
        val connection = URL("$endpoint${separator}resource=api/v1/bootstrap")
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("X-STK-Request-Id", UUID.randomUUID().toString())
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = runCatching { JSONObject(stream?.bufferedReader()?.use { it.readText() }.orEmpty()) }
                .getOrElse { throw AuthException(5000, "服务器返回格式错误") }
            val code = response.optInt("code", 5000)
            if (code != 0) throw AuthException(
                code,
                response.optString("message", "启动配置加载失败"),
                response.optString("request_id").takeIf(String::isNotBlank),
            )
            parseAuthBootstrap(response.optJSONObject("data") ?: JSONObject())
        } catch (error: AuthException) {
            throw error
        } catch (error: Exception) {
            throw AuthException(5001, error.message ?: "无法连接服务器")
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun createCaptcha(scene: String): CaptchaChallenge {
        val data = post("captcha_create", JSONObject().put("scene", scene))
        val challengeId = data.getString("challenge_id")
        val endpointUrl = URL(endpoint)
        val fallbackImageUrl = URL(
            endpointUrl.protocol,
            endpointUrl.host,
            endpointUrl.port,
            "/plugin.php?id=stk_auth:captcha&challenge=$challengeId",
        ).toString()
        val imageUrl = data.optString("image_url").ifBlank { fallbackImageUrl }
        val safeImageUrl = imageUrl.takeIf { value ->
            runCatching {
                val url = URL(value)
                url.protocol == "https" && url.host.equals(endpointUrl.host, ignoreCase = true)
            }.getOrDefault(false)
        }
        return CaptchaChallenge(
            challengeId = challengeId,
            prompt = data.optString("prompt", "请输入安全验证码"),
            imageUrl = safeImageUrl,
            imageBytes = safeImageUrl?.let { downloadCaptchaImage(it) },
            expiresAtMillis = System.currentTimeMillis() +
                data.optLong("expires_in", 300L).coerceIn(60L, 600L) * 1_000L,
        )
    }

    private suspend fun downloadCaptchaImage(imageUrl: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.setRequestProperty("Accept", "image/png")
                if (connection.responseCode !in 200..299) return@runCatching null
                if (!connection.contentType.orEmpty().startsWith("image/png")) return@runCatching null
                val bytes = connection.inputStream.use { it.readBytes() }
                bytes.takeIf { it.isNotEmpty() && it.size <= 256 * 1024 }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    override suspend fun passwordLogin(
        mobile: String,
        password: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        val ticket = verifyCaptcha("password_login", captchaChallenge, captchaCode)
        return authenticate(
            "password_login",
            JSONObject().put("mobile", mobile).put("password", password).put("captcha_ticket", ticket),
            persistSession = false,
        )
    }

    override suspend fun sendSms(
        mobile: String,
        captchaChallenge: String,
        captchaCode: String,
    ): SmsSendResult {
        val ticket = verifyCaptcha("sms_send", captchaChallenge, captchaCode)
        val data = post("sms_send", JSONObject().put("mobile", mobile).put("captcha_ticket", ticket))
        return SmsSendResult(
            retryAfterSeconds = data.optInt("retry_after_seconds", 60),
            debugCode = data.optString("debug_code").takeIf { it.isNotBlank() },
        )
    }

    override suspend fun smsLogin(
        mobile: String,
        smsCode: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        val ticket = verifyCaptcha("sms_login", captchaChallenge, captchaCode)
        return authenticate(
            "sms_login",
            JSONObject().put("mobile", mobile).put("sms_code", smsCode).put("captcha_ticket", ticket),
            persistSession = false,
        )
    }

    override suspend fun register(
        mobile: String,
        password: String,
        confirmPassword: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        val ticket = verifyCaptcha("register", captchaChallenge, captchaCode)
        return authenticate(
            "register",
            JSONObject().put("mobile", mobile).put("password", password).put("confirm_password", confirmPassword).put("captcha_ticket", ticket),
            persistSession = false,
        )
    }

    override suspend fun activateSession(session: AuthSession) {
        tokenStore.save(session)
    }

    override suspend fun refreshSession(refreshToken: String): AuthSession =
        authenticate("refresh", JSONObject().put("refresh_token", refreshToken))

    override suspend fun logout() {
        val result = runCatching { post("logout", JSONObject()) }
        tokenStore.clear()
        result.fold(
            onSuccess = { tokenStore.clearPendingRevocation() },
            onFailure = { error ->
                tokenStore.markRevocationPending()
                throw when (error) {
                    is AuthException -> error
                    else -> AuthException(5001, error.message ?: "服务器撤销失败")
                }
            },
        )
    }

    private suspend fun authenticate(action: String, body: JSONObject, persistSession: Boolean = true): AuthSession {
        val data = post(action, body)
        val userJson = data.getJSONObject("user")
        val session = AuthSession(
            accessToken = data.getString("access_token"),
            refreshToken = data.getString("refresh_token"),
            expiresAt = data.getLong("expires_at"),
            user = UserSummary(
                uid = userJson.getLong("uid"),
                username = userJson.getString("username"),
                mobileMasked = userJson.getString("mobile_masked"),
                memberLabel = userJson.optString("member_label", "普通用户"),
            ),
        )
        if (persistSession) tokenStore.save(session)
        return session
    }

    private suspend fun verifyCaptcha(scene: String, challenge: String, code: String): String {
        val data = post(
            "captcha_verify",
            JSONObject().put("scene", scene).put("captcha_challenge", challenge).put("captcha_code", code),
        )
        return data.getString("captcha_ticket")
    }

    private suspend fun post(action: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val separator = if (endpoint.contains('?')) "&" else "?"
        val connection = URL("$endpoint${separator}action=$action")
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("X-STK-Request-Id", UUID.randomUUID().toString())
            val state = tokenStore.session.first()
            if (state is SessionState.LoggedIn) connection.setRequestProperty("Authorization", "Bearer ${state.session.accessToken}")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
            val response = runCatching { JSONObject(responseText) }
                .getOrElse { throw AuthException(5000, "服务器返回格式错误") }
            val code = response.optInt("code", 5000)
            if (code != 0) {
                if (code in setOf(4010, 4011)) tokenStore.clear()
                throw AuthException(
                    code,
                    response.optString("message", "请求失败"),
                    response.optString("request_id").takeIf(String::isNotBlank),
                )
            }
            response.optJSONObject("data") ?: JSONObject()
        } catch (error: AuthException) {
            throw error
        } catch (error: Exception) {
            throw AuthException(5001, error.message ?: "无法连接服务器")
        } finally {
            connection.disconnect()
        }
    }
}

internal fun parseAuthBootstrap(data: JSONObject): AuthBootstrapConfig {
    val auth = data.optJSONObject("auth") ?: JSONObject()
    return AuthBootstrapConfig(
        passwordEnabled = auth.optBoolean("password_enabled", true),
        smsEnabled = auth.optBoolean("sms_enabled", true),
        registerEnabled = auth.optBoolean("register_enabled", true),
        defaultLoginMode = auth.optString("default_login_tab", "password").takeIf { it in setOf("password", "sms") } ?: "password",
        loginSuccessRoute = normalizeAuthSuccessRoute(auth.optString("login_success_route", "stk://home")),
        registerSuccessRoute = normalizeAuthSuccessRoute(auth.optString("register_success_route", "stk://home")),
    )
}

internal fun normalizeAuthSuccessRoute(route: String): String = when (route.trim().lowercase()) {
    "stk://home" -> "stk://home"
    "stk://publish" -> "stk://publish"
    "stk://me" -> "stk://me"
    "stk://me/projects" -> "stk://me/projects"
    else -> "stk://home"
}
