package com.zzyihao.stk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zzyihao.stk.data.auth.AuthBootstrapConfig
import com.zzyihao.stk.ui.auth.AuthOperationState
import com.zzyihao.stk.ui.auth.LoginScreen
import com.zzyihao.stk.ui.theme.StkTheme
import org.junit.Rule
import org.junit.Test

class LoginModeInteractionTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun smsDefaultShowsSmsFormAndCanSwitchToPassword() {
        composeRule.setContent { StkTheme { login(AuthBootstrapConfig(defaultLoginMode = "sms")) } }
        composeRule.onNodeWithText("短信验证码").assertIsDisplayed()
        composeRule.onNodeWithText("密码登录").performClick()
        composeRule.onNodeWithText("登录密码").assertIsDisplayed()
    }

    @Test
    fun disabledSmsModeExplainsWhyItIsUnavailable() {
        composeRule.setContent { StkTheme { login(AuthBootstrapConfig(smsEnabled = false, defaultLoginMode = "sms")) } }
        composeRule.onNodeWithText("登录密码").assertIsDisplayed()
        composeRule.onNodeWithText("短信验证码").assertDoesNotExist()
        composeRule.onNodeWithText("短信登录").performClick()
        composeRule.onNodeWithText("短信登录不可用").assertIsDisplayed()
    }

    @Test
    fun passwordRecoveryDoesNotRecommendDisabledSmsLogin() {
        composeRule.setContent { StkTheme { login(AuthBootstrapConfig(smsEnabled = false)) } }
        composeRule.onNodeWithText("忘记密码").performClick()
        composeRule.onNodeWithText("当前版本尚未提供自助找回密码，短信登录也尚未配置。请联系管理员协助处理。")
            .assertIsDisplayed()
    }

    @androidx.compose.runtime.Composable
    private fun login(config: AuthBootstrapConfig) = LoginScreen(
        operation = AuthOperationState(),
        bootstrap = config,
        onOpenCaptcha = {},
        onPasswordLogin = { _, _, _ -> },
        onSendSms = { _, _ -> },
        onSmsLogin = { _, _, _ -> },
        onCloseCaptcha = {},
        onRegister = {},
        onOpenLegal = {},
        onClearMessage = {},
    )
}
