package com.zzyihao.stk.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorsTest {
    @Test
    fun validMobileHasNoError() {
        assertNull(AuthValidators.mobileError("13800138000"))
    }

    @Test
    fun invalidMobileReturnsError() {
        assertEquals("请输入正确的 11 位手机号", AuthValidators.mobileError("123"))
    }

    @Test
    fun passwordRequiresEightCharacters() {
        assertEquals("密码至少 8 位", AuthValidators.passwordError("1234567"))
        assertNull(AuthValidators.passwordError("12345678"))
    }

    @Test
    fun confirmPasswordMustMatch() {
        assertEquals("两次输入的密码不一致", AuthValidators.confirmPasswordError("123456", "654321"))
        assertNull(AuthValidators.confirmPasswordError("123456", "123456"))
    }

    @Test
    fun smsCodeMustContainSixDigits() {
        assertEquals("请输入 6 位短信验证码", AuthValidators.smsCodeError("12345"))
        assertEquals("请输入 6 位短信验证码", AuthValidators.smsCodeError("12a456"))
        assertNull(AuthValidators.smsCodeError("123456"))
    }

    @Test
    fun captchaCodeMustContainFourDigits() {
        assertEquals("请输入图片中的 4 位数字", AuthValidators.captchaCodeError("安全验证"))
        assertEquals("请输入图片中的 4 位数字", AuthValidators.captchaCodeError("123"))
        assertNull(AuthValidators.captchaCodeError("1234"))
    }
}
