package com.zzyihao.stk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zzyihao.stk.ui.auth.AuthOperationState
import com.zzyihao.stk.ui.auth.RegisterScreen
import com.zzyihao.stk.ui.theme.StkTheme
import org.junit.Rule
import org.junit.Test

class RegisterInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun agreementCanBeUncheckedAndIsRequired() {
        composeRule.setContent {
            StkTheme {
                RegisterScreen(
                    operation = AuthOperationState(),
                    onOpenCaptcha = {},
                    onRegister = { _, _, _, _ -> },
                    onCloseCaptcha = {},
                    onBackToLogin = {},
                    onOpenLegal = {},
                    onClearMessage = {},
                    onCompleteRegistration = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("同意协议").performClick()
        composeRule.onNodeWithText("注册").performClick()
        composeRule.onNodeWithText("请先阅读并同意协议").assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityToggleIsAvailable() {
        composeRule.setContent {
            StkTheme {
                RegisterScreen(
                    operation = AuthOperationState(),
                    onOpenCaptcha = {},
                    onRegister = { _, _, _, _ -> },
                    onCloseCaptcha = {},
                    onBackToLogin = {},
                    onOpenLegal = {},
                    onClearMessage = {},
                    onCompleteRegistration = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("显示密码").assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("隐藏密码").assertIsDisplayed()
    }
}
