package com.zzyihao.stk.data.auth

interface AuthRepository {
    suspend fun getBootstrap(): AuthBootstrapConfig

    suspend fun createCaptcha(scene: String): CaptchaChallenge

    suspend fun passwordLogin(
        mobile: String,
        password: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession

    suspend fun sendSms(
        mobile: String,
        captchaChallenge: String,
        captchaCode: String,
    ): SmsSendResult

    suspend fun smsLogin(
        mobile: String,
        smsCode: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession

    suspend fun register(
        mobile: String,
        password: String,
        confirmPassword: String,
        captchaChallenge: String,
        captchaCode: String,
    ): AuthSession

    suspend fun activateSession(session: AuthSession)

    suspend fun refreshSession(refreshToken: String): AuthSession

    suspend fun logout()
}
