package com.zzyihao.stk.data.auth

object AuthValidators {
    private val mobilePattern = Regex("^1[3-9]\\d{9}$")

    fun mobileError(value: String): String? = when {
        value.isBlank() -> "请输入手机号"
        !mobilePattern.matches(value) -> "请输入正确的 11 位手机号"
        else -> null
    }

    fun passwordError(value: String): String? = when {
        value.isBlank() -> "请输入登录密码"
        value.length < 8 -> "密码至少 8 位"
        value.length > 64 -> "密码最多 64 位"
        else -> null
    }

    fun smsCodeError(value: String): String? = when {
        value.isBlank() -> "请输入短信验证码"
        !Regex("^\\d{6}$").matches(value) -> "请输入 6 位短信验证码"
        else -> null
    }

    fun captchaCodeError(value: String): String? = when {
        value.isBlank() -> "请输入图片中的 4 位数字"
        !Regex("^\\d{4}$").matches(value) -> "请输入图片中的 4 位数字"
        else -> null
    }

    fun confirmPasswordError(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "请再次输入密码"
        password != confirm -> "两次输入的密码不一致"
        else -> null
    }
}
