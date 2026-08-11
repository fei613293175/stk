package com.zzyihao.stk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzyihao.stk.data.auth.parseAuthBootstrap
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthBootstrapInstrumentedTest {
    @Test
    fun parserAppliesSwitchesModesAndSafeRoutes() {
        val config = parseAuthBootstrap(JSONObject("""{
          "auth": {
            "password_enabled": false,
            "sms_enabled": true,
            "register_enabled": false,
            "default_login_tab": "sms",
            "login_success_route": "stk://me",
            "register_success_route": "https://unsafe.example"
          }
        }"""))

        assertFalse(config.passwordEnabled)
        assertTrue(config.smsEnabled)
        assertFalse(config.registerEnabled)
        assertEquals("sms", config.defaultLoginMode)
        assertEquals("stk://me", config.loginSuccessRoute)
        assertEquals("stk://home", config.registerSuccessRoute)
    }

    @Test
    fun parserUsesContractDefaultsForMissingAndInvalidValues() {
        val config = parseAuthBootstrap(JSONObject("""{"auth":{"default_login_tab":"other"}}"""))
        assertTrue(config.passwordEnabled)
        assertTrue(config.smsEnabled)
        assertTrue(config.registerEnabled)
        assertEquals("password", config.defaultLoginMode)
        assertEquals("stk://home", config.loginSuccessRoute)
    }
}
