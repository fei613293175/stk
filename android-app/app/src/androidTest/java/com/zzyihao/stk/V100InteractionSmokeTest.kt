package com.zzyihao.stk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V100InteractionSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun unauthenticatedEntryControlsAreRenderedAndRecorded() {
        val tags = listOf(
            "login_phone", "login_password", "login_password_submit",
            "login_mode_sms", "login_open_register", "login_open_reset",
            "login_open_agreement", "login_open_privacy"
        )
        val passed = JSONArray()
        tags.forEach { tag ->
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
            passed.put(tag)
        }
        writeEvidence(passed)
    }

    private fun writeEvidence(passedTags: JSONArray) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val ids = mutableListOf<String>()
        val tagToId = mapOf(
            "login_phone" to "INT-AUTH-003", "login_password" to "INT-AUTH-004",
            "login_password_submit" to "INT-AUTH-008", "login_mode_sms" to "INT-AUTH-002",
            "login_open_register" to "INT-AUTH-010", "login_open_reset" to "INT-AUTH-011",
            "login_open_agreement" to "INT-AUTH-012", "login_open_privacy" to "INT-AUTH-013"
        )
        for (index in 0 until passedTags.length()) ids += tagToId[passedTags.getString(index)]!!
        val output = JSONObject().put("passed_interaction_ids", JSONArray(ids)).put("failed_interaction_ids", JSONArray())
        context.getExternalFilesDir(null)!!.resolve("interaction-results.json").writeText(output.toString())
    }
}
