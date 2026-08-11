package com.zzyihao.stk.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zzyihao.stk.data.auth.AuthException
import com.zzyihao.stk.data.auth.AuthRepository
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.auth.CaptchaChallenge
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthOperation {
    Idle,
    CaptchaLoading,
    CaptchaRefreshing,
    CaptchaExpired,
    CaptchaSuccess,
    PasswordLogin,
    SmsSending,
    SmsLogin,
    Registering,
    ActivatingRegistration,
    LoginSuccess,
}

enum class AuthIssue { None, Validation, RateLimited, Restricted, Network, Timeout, Server }

enum class AuthRisk { None, LoginLocked, AccountDisabled, ManualReview, MethodUnavailable }

private const val LOGIN_SUCCESS_FEEDBACK_MILLIS = 700L

data class AuthOperationState(
    val operation: AuthOperation = AuthOperation.Idle,
    val message: String? = null,
    val isError: Boolean = false,
    val issue: AuthIssue = AuthIssue.None,
    val risk: AuthRisk = AuthRisk.None,
    val captcha: CaptchaChallenge? = null,
    val captchaVisible: Boolean = false,
    val captchaAttemptCount: Int = 0,
    val smsCountdown: Int = 0,
    val retryCountdown: Int = 0,
    val errorCode: Int? = null,
    val requestId: String? = null,
    val activeScene: String? = null,
    val pendingRegistrationSession: AuthSession? = null,
) {
    val loading: Boolean
        get() = operation !in setOf(
            AuthOperation.Idle,
            AuthOperation.CaptchaExpired,
            AuthOperation.CaptchaSuccess,
            AuthOperation.LoginSuccess,
        )
}

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthOperationState())
    val state: StateFlow<AuthOperationState> = _state.asStateFlow()
    private var countdownJob: Job? = null
    private var retryCountdownJob: Job? = null

    fun openCaptcha(scene: String) {
        val action = if (_state.value.captcha == null) AuthOperation.CaptchaLoading else AuthOperation.CaptchaRefreshing
        execute(action = action, scene = scene) {
            val challenge = repository.createCaptcha(scene)
            _state.update {
                it.copy(
                    operation = AuthOperation.Idle,
                    captcha = challenge,
                    captchaVisible = true,
                    captchaAttemptCount = 0,
                    message = null,
                    isError = false,
                    issue = AuthIssue.None,
                    errorCode = null,
                )
            }
        }
    }

    fun closeCaptcha() {
        _state.update {
            it.copy(
                operation = AuthOperation.Idle,
                captcha = null,
                captchaVisible = false,
                message = null,
                isError = false,
                issue = AuthIssue.None,
                risk = AuthRisk.None,
                errorCode = null,
            )
        }
    }

    fun passwordLogin(mobile: String, password: String, code: String) {
        val challenge = _state.value.captcha ?: return
        if (handleExpiredCaptcha(challenge, "password_login")) return
        execute(
            action = AuthOperation.PasswordLogin,
            scene = "password_login",
            captchaOnValidationError = challenge,
        ) {
            val session = repository.passwordLogin(mobile, password, challenge.challengeId, code)
            showCaptchaSuccess(challenge)
            _state.value = AuthOperationState(
                operation = AuthOperation.LoginSuccess,
                message = "登录成功，正在进入首页",
                captcha = null,
                activeScene = "password_login",
            )
            delay(LOGIN_SUCCESS_FEEDBACK_MILLIS)
            repository.activateSession(session)
        }
        scheduleCaptchaDismiss(AuthOperation.PasswordLogin, challenge.challengeId)
    }

    fun sendSms(mobile: String, code: String) {
        val challenge = _state.value.captcha ?: return
        if (handleExpiredCaptcha(challenge, "sms_send")) return
        execute(
            action = AuthOperation.SmsSending,
            scene = "sms_send",
            captchaOnValidationError = challenge,
        ) {
            val result = repository.sendSms(mobile, challenge.challengeId, code)
            showCaptchaSuccess(challenge)
            _state.value = AuthOperationState(
                message = "验证码已发送，有效期 5 分钟。",
                smsCountdown = result.retryAfterSeconds,
                captcha = null,
                activeScene = "sms_send",
            )
            startCountdown(result.retryAfterSeconds)
        }
        scheduleCaptchaDismiss(AuthOperation.SmsSending, challenge.challengeId)
    }

    fun smsLogin(mobile: String, smsCode: String, captchaCode: String) {
        val challenge = _state.value.captcha ?: return
        if (handleExpiredCaptcha(challenge, "sms_login")) return
        execute(
            action = AuthOperation.SmsLogin,
            scene = "sms_login",
            captchaOnValidationError = challenge,
        ) {
            val session = repository.smsLogin(mobile, smsCode, challenge.challengeId, captchaCode)
            showCaptchaSuccess(challenge)
            _state.value = AuthOperationState(
                operation = AuthOperation.LoginSuccess,
                message = "登录成功，正在进入首页",
                captcha = null,
                activeScene = "sms_login",
            )
            delay(LOGIN_SUCCESS_FEEDBACK_MILLIS)
            repository.activateSession(session)
        }
        scheduleCaptchaDismiss(AuthOperation.SmsLogin, challenge.challengeId)
    }

    fun register(mobile: String, password: String, confirmPassword: String, code: String) {
        val challenge = _state.value.captcha ?: return
        if (handleExpiredCaptcha(challenge, "register")) return
        execute(
            action = AuthOperation.Registering,
            scene = "register",
            captchaOnValidationError = challenge,
        ) {
            val session = repository.register(mobile, password, confirmPassword, challenge.challengeId, code)
            showCaptchaSuccess(challenge)
            _state.value = AuthOperationState(captcha = null, pendingRegistrationSession = session)
        }
        scheduleCaptchaDismiss(AuthOperation.Registering, challenge.challengeId)
    }

    fun completeRegistration() {
        val session = _state.value.pendingRegistrationSession ?: return
        execute(action = AuthOperation.ActivatingRegistration, scene = "register") {
            repository.activateSession(session)
            _state.value = AuthOperationState(message = "注册成功")
        }
    }

    fun clearMessage() {
        _state.update {
            if (it.isError) {
                it.copy(
                    message = null,
                    isError = false,
                    issue = AuthIssue.None,
                    risk = AuthRisk.None,
                    errorCode = null,
                    requestId = null,
                    retryCountdown = 0,
                )
            } else {
                it
            }
        }
    }

    private fun execute(
        action: AuthOperation,
        scene: String,
        captchaOnValidationError: CaptchaChallenge? = null,
        block: suspend () -> Unit,
    ) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    operation = action,
                    activeScene = scene,
                    message = null,
                    isError = false,
                    issue = AuthIssue.None,
                    risk = AuthRisk.None,
                    errorCode = null,
                    requestId = null,
                    retryCountdown = 0,
                    captchaVisible = if (action in setOf(
                            AuthOperation.CaptchaLoading,
                            AuthOperation.CaptchaRefreshing,
                        )
                    ) true else it.captchaVisible,
                )
            }
            try {
                block()
            } catch (error: AuthException) {
                val issue = classifyIssue(error)
                val risk = classifyRisk(error)
                val retrySeconds = if (issue == AuthIssue.RateLimited) retrySeconds(error.message) else 0
                if (scene == "sms_login" && error.code in setOf(4003, 4004)) countdownJob?.cancel()
                val captchaAttemptCount = if (error.code == 4002) _state.value.captchaAttemptCount + 1 else 0
                _state.update {
                    it.copy(
                        message = displayMessage(error, scene, issue, retrySeconds, captchaAttemptCount),
                        isError = true,
                        issue = issue,
                        risk = risk,
                        errorCode = error.code,
                        requestId = error.requestId,
                        retryCountdown = retrySeconds,
                        smsCountdown = if (scene == "sms_login" && error.code in setOf(4003, 4004)) 0 else it.smsCountdown,
                        captcha = if (error.code == 4002) captchaOnValidationError else it.captcha,
                        captchaVisible = if (error.code == 4002) true else action in setOf(
                            AuthOperation.CaptchaLoading,
                            AuthOperation.CaptchaRefreshing,
                        ),
                        captchaAttemptCount = captchaAttemptCount,
                    )
                }
                if (retrySeconds > 0) startRetryCountdown(retrySeconds)
                if (error.code == 4002 && captchaAttemptCount >= 5) {
                    viewModelScope.launch {
                        delay(600)
                        openCaptcha(scene)
                    }
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        message = "网络连接失败，已保留输入内容",
                        isError = true,
                        issue = AuthIssue.Network,
                        captchaVisible = action in setOf(AuthOperation.CaptchaLoading, AuthOperation.CaptchaRefreshing),
                    )
                }
            } finally {
                _state.update { current ->
                    if (current.operation == action) current.copy(operation = AuthOperation.Idle) else current
                }
            }
        }
    }

    private fun classifyIssue(error: AuthException): AuthIssue = when {
        error.code == 4290 -> AuthIssue.RateLimited
        error.code == 4030 -> AuthIssue.Restricted
        error.code == 5001 && error.message.containsTimeout() -> AuthIssue.Timeout
        error.code == 5001 -> AuthIssue.Network
        error.code in setOf(4001, 4002, 4003, 4004, 4005, 4093) -> AuthIssue.Validation
        else -> AuthIssue.Server
    }

    private fun classifyRisk(error: AuthException): AuthRisk = when {
        error.code == 4290 && error.message.contains("登录尝试") -> AuthRisk.LoginLocked
        error.code == 4030 && error.message.contains("禁用") -> AuthRisk.AccountDisabled
        error.code == 4030 && listOf("人工", "核验", "受限").any(error.message::contains) -> AuthRisk.ManualReview
        error.code == 4030 -> AuthRisk.MethodUnavailable
        else -> AuthRisk.None
    }

    private fun displayMessage(
        error: AuthException,
        scene: String,
        issue: AuthIssue,
        retrySeconds: Int,
        captchaAttemptCount: Int,
    ): String = when {
        error.code == 4002 && captchaAttemptCount < 5 ->
            "验证码错误，还可尝试 ${(5 - captchaAttemptCount).coerceAtLeast(0)} 次"
        error.code == 4002 -> "验证码已失效，正在刷新"
        issue == AuthIssue.RateLimited && scene == "register" ->
            "注册请求过于频繁，请 ${retrySeconds.coerceAtLeast(1)} 秒后重试。"
        issue == AuthIssue.RateLimited -> "操作过于频繁，请 ${retrySeconds.coerceAtLeast(1)} 秒后重试。"
        issue == AuthIssue.Restricted -> "暂时无法登录，需要进一步核验。"
        issue == AuthIssue.Timeout -> "请求超时，请稍后重试"
        issue == AuthIssue.Network && scene == "register" -> "网络连接失败，表单内容已保留"
        issue == AuthIssue.Network -> "网络连接失败，已保留输入内容"
        scene == "register" && error.code == 4005 -> "该手机号已注册，可直接登录或找回密码"
        scene == "register" && issue == AuthIssue.Server ->
            error.requestId?.let { "注册失败，请求编号 $it。" } ?: "注册失败，请稍后重试。"
        scene == "password_login" && error.code == 4003 -> "手机号或密码错误"
        scene == "sms_login" && error.code == 4003 -> "短信验证码错误，请重新输入"
        scene == "sms_login" && error.code == 4004 -> "验证码已过期，请重新发送"
        else -> error.message
    }

    private fun retrySeconds(message: String): Int =
        Regex("(\\d+)\\s*秒").find(message)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 60

    private fun String.containsTimeout(): Boolean {
        val normalized = lowercase()
        return "超时" in this || "timed out" in normalized || "timeout" in normalized
    }

    private fun handleExpiredCaptcha(challenge: CaptchaChallenge, scene: String): Boolean {
        if (challenge.expiresAtMillis > System.currentTimeMillis()) return false
        _state.update {
            it.copy(
                operation = AuthOperation.CaptchaExpired,
                activeScene = scene,
                captcha = challenge,
                captchaVisible = true,
                message = "验证码已过期，已为您刷新",
                isError = true,
                issue = AuthIssue.Validation,
                errorCode = 4002,
            )
        }
        viewModelScope.launch {
            delay(600)
            openCaptcha(scene)
        }
        return true
    }

    private suspend fun showCaptchaSuccess(challenge: CaptchaChallenge) {
        _state.update {
            it.copy(
                operation = AuthOperation.CaptchaSuccess,
                captcha = challenge,
                captchaVisible = true,
                message = null,
                isError = false,
                issue = AuthIssue.None,
                errorCode = null,
            )
        }
        delay(250)
    }

    private fun scheduleCaptchaDismiss(action: AuthOperation, challengeId: String) {
        viewModelScope.launch {
            delay(350)
            _state.update { current ->
                if (current.operation == action && current.captcha?.challengeId == challengeId) {
                    current.copy(captchaVisible = false)
                } else {
                    current
                }
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (value in seconds downTo 1) {
                _state.update { it.copy(smsCountdown = value) }
                delay(1_000)
            }
            _state.update { it.copy(smsCountdown = 0) }
        }
    }

    private fun startRetryCountdown(seconds: Int) {
        retryCountdownJob?.cancel()
        retryCountdownJob = viewModelScope.launch {
            for (value in seconds downTo 1) {
                _state.update { current ->
                    if (current.issue == AuthIssue.RateLimited) {
                        current.copy(
                            retryCountdown = value,
                            message = if (current.activeScene == "register") {
                                "注册请求过于频繁，请 $value 秒后重试。"
                            } else {
                                "操作过于频繁，请 $value 秒后重试。"
                            },
                        )
                    } else {
                        current
                    }
                }
                delay(1_000)
            }
            _state.update { current ->
                if (current.issue == AuthIssue.RateLimited) {
                    current.copy(retryCountdown = 0, message = "现在可以重试。")
                } else {
                    current
                }
            }
        }
    }

    class Factory(
        private val repository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AuthViewModel::class.java))
            return AuthViewModel(repository) as T
        }
    }
}
