package com.zzyihao.stk.data.auth

data class UserSummary(
    val uid: Long,
    val username: String,
    val mobileMasked: String,
    val memberLabel: String,
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val user: UserSummary,
)

data class CaptchaChallenge(
    val challengeId: String,
    val prompt: String,
    val imageUrl: String? = null,
    val imageBytes: ByteArray? = null,
    val expiresAtMillis: Long = Long.MAX_VALUE,
)

data class SmsSendResult(
    val retryAfterSeconds: Int,
    val debugCode: String? = null,
)

data class AuthBootstrapConfig(
    val passwordEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val registerEnabled: Boolean = true,
    val defaultLoginMode: String = "password",
    val loginSuccessRoute: String = "stk://home",
    val registerSuccessRoute: String = "stk://home",
)

class AuthException(
    val code: Int,
    override val message: String,
    val requestId: String? = null,
) : Exception(message)
