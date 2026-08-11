package com.zzyihao.stk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AuthSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginEntryIsVisible() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("手机号").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("商推客").assertIsDisplayed()
        composeRule.onNodeWithText("手机号").assertIsDisplayed()
    }
}
