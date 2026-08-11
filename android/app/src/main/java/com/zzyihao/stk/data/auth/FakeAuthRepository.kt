package com.zzyihao.stk.data.auth

import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.delay
import java.util.UUID

class FakeAuthRepository(
    private val tokenStore: TokenStore,
) : AuthRepository {
    private var lastSession: AuthSession? = null
    private var lastMobile: String? = null

    override suspend fun getBootstrap(): AuthBootstrapConfig = AuthBootstrapConfig()

    override suspend fun createCaptcha(scene: String): CaptchaChallenge {
        delay(150)
        return CaptchaChallenge(
            challengeId = "dev-$scene",
            // In the fixture backend the challenge is intentionally visible.
            // Keep the answer itself in the image area; the dialog title is
            // not a value that can satisfy validation.
            prompt = "2468",
            expiresAtMillis = System.currentTimeMillis() + 300_000L,
        )
    }

    override suspend fun passwordLogin(
        mobile: String,
        password: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        delay(350)
        validateMobile(mobile)
        validateCaptcha(captchaChallenge, captchaCode)
        if (password != "12345678") {
            throw AuthException(4003, "手机号或密码错误")
        }
        return createSession(mobile, persist = false)
    }

    override suspend fun sendSms(
        mobile: String,
        captchaChallenge: String,
        captchaCode: String,
    ): SmsSendResult {
        delay(300)
        validateMobile(mobile)
        validateCaptcha(captchaChallenge, captchaCode)
        return SmsSendResult(retryAfterSeconds = 60, debugCode = "123456")
    }

    override suspend fun smsLogin(
        mobile: String,
        smsCode: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        delay(350)
        validateMobile(mobile)
        validateCaptcha(captchaChallenge, captchaCode)
        if (smsCode != "123456") {
            throw AuthException(4004, "短信验证码错误或已失效")
        }
        return createSession(mobile, persist = false)
    }

    override suspend fun register(
        mobile: String,
        password: String,
        confirmPassword: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession {
        delay(400)
        validateMobile(mobile)
        validateCaptcha(captchaChallenge, captchaCode)
        AuthValidators.passwordError(password)?.let { throw AuthException(4001, it) }
        if (password != confirmPassword) {
            throw AuthException(4001, "两次输入的密码不一致")
        }
        return createSession(mobile, persist = false)
    }

    override suspend fun activateSession(session: AuthSession) {
        lastSession = session
        tokenStore.save(session)
    }

    override suspend fun refreshSession(refreshToken: String): AuthSession {
        val session = lastSession?.takeIf { it.refreshToken == refreshToken }
            ?: throw AuthException(4011, "刷新令牌无效或已过期")
        return createSession(lastMobile ?: throw AuthException(4011, "刷新令牌无效或已过期"), persist = true)
    }

    override suspend fun logout() {
        tokenStore.clear()
        tokenStore.clearPendingRevocation()
    }

    private fun validateMobile(mobile: String) {
        AuthValidators.mobileError(mobile)?.let { throw AuthException(4001, it) }
    }

    private fun validateCaptcha(challenge: String, code: String) {
        if (!challenge.startsWith("dev-") || code != "2468") {
            throw AuthException(4002, "安全验证码错误或已失效")
        }
    }

    private suspend fun createSession(mobile: String, persist: Boolean): AuthSession {
        val suffix = mobile.takeLast(4)
        val sessionId = UUID.randomUUID().toString()
        val session = AuthSession(
            accessToken = "dev-access-$sessionId",
            refreshToken = "dev-refresh-$sessionId",
            expiresAt = (System.currentTimeMillis() / 1000L) + 1800L,
            user = UserSummary(
                uid = 10000L + suffix.toLongOrNull().orZero(),
                username = "商推客用户$suffix",
                mobileMasked = mobile.take(3) + "****" + suffix,
                memberLabel = "普通用户",
            ),
        )
        lastSession = session
        lastMobile = mobile
        if (persist) tokenStore.save(session)
        return session
    }

    private fun Long?.orZero(): Long = this ?: 0L
}
