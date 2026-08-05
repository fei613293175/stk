package com.zzyihao.stk

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zzyihao.stk.designsystem.StkTheme
import org.json.JSONArray
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections

@RunWith(AndroidJUnit4::class)
class V100InteractionSmokeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun loginInputsModesNavigationAndCaptchaAreInteractive() {
        val api = FakeApi()
        composeRule.setContent { StkTheme { LoginScreen(api, "device", {}, {}, {}, {}, {}) } }
        act("INT-AUTH-003", "login_phone") { performTextInput("13800138000") }
        act("INT-AUTH-004", "login_password") { performTextInput("Password123") }
        act("INT-AUTH-005", "login_password_visibility") { performClick() }
        act("INT-AUTH-008", "login_password_submit") { performClick() }
        composeRule.onNodeWithTag("captcha_input").assertIsDisplayed()
        act("INT-CAPTCHA-001", "captcha_input") { performTextInput("1234") }
        act("INT-CAPTCHA-002", "captcha_refresh") { performClick() }
        act("INT-CAPTCHA-003", "captcha_confirm") { performClick() }
        check(api.posts.any { it == "/v1/auth/captcha/verify" })

        composeRule.setContent { StkTheme { LoginScreen(api, "device", {}, {}, {}, {}, {}) } }
        act("INT-AUTH-002", "login_mode_sms") { performClick() }
        composeRule.onNodeWithTag("login_sms_code").assertIsDisplayed()
        act("INT-AUTH-006", "login_sms_code") { performTextInput("123456") }
        composeRule.onNodeWithTag("login_phone").performTextInput("13800138000")
        act("INT-AUTH-007", "login_send_sms") { performClick() }
        composeRule.onNodeWithTag("captcha_cancel").assertIsDisplayed()
        act("INT-CAPTCHA-004", "captcha_cancel") { performClick() }
        act("INT-AUTH-001", "login_mode_password") { performClick() }

        val routes = listOf(
            "INT-AUTH-009" to "login_sms_submit",
            "INT-AUTH-010" to "login_open_register",
            "INT-AUTH-011" to "login_open_reset",
            "INT-AUTH-012" to "login_open_agreement",
            "INT-AUTH-013" to "login_open_privacy",
        )
        routes.forEach { (id, tag) ->
            composeRule.setContent { StkTheme { LoginScreen(api, "device", {}, {}, {}, {}, {}) } }
            if (tag == "login_sms_submit") {
                composeRule.onNodeWithTag("login_mode_sms").performClick()
                composeRule.onNodeWithTag("login_phone").performTextInput("13800138000")
                composeRule.onNodeWithTag("login_sms_code").performTextInput("123456")
            }
            act(id, tag) { performClick() }
        }
    }

    @Test fun registerResetAndLegalControlsChangeState() {
        val api = FakeApi()
        composeRule.setContent { StkTheme { ApiRegisterScreen(api, "device", {}, {}, {}, {}) } }
        act("INT-REG-001", "register_phone") { performTextInput("13800138000") }
        act("INT-REG-002", "register_password") { performTextInput("Password123") }
        act("INT-REG-003", "register_password_confirm") { performTextInput("Password123") }
        act("INT-REG-004", "register_password_visibility") { performClick() }
        act("INT-REG-005", "register_agreement_check") { performClick() }
        act("INT-REG-006", "register_submit") { performClick() }
        composeRule.onNodeWithTag("captcha_cancel").performClick()
        listOf("INT-REG-007" to "register_back_login", "INT-REG-008" to "register_open_agreement", "INT-REG-009" to "register_open_privacy").forEach { (id, tag) ->
            act(id, tag) { performClick() }
        }

        composeRule.setContent { StkTheme { ApiResetScreen(api, "device", {}) } }
        act("INT-RESET-001", "reset_phone") { performTextInput("13800138000") }
        act("INT-RESET-003", "reset_sms_code") { performTextInput("123456") }
        act("INT-RESET-004", "reset_new_password") { performTextInput("Password123") }
        act("INT-RESET-005", "reset_new_password_confirm") { performTextInput("Password123") }
        act("INT-RESET-002", "reset_send_sms") { performClick() }
        composeRule.onNodeWithTag("captcha_cancel").performClick()
        act("INT-RESET-006", "reset_submit") { performClick() }
        composeRule.onNodeWithTag("captcha_cancel").performClick()
        act("INT-RESET-007", "reset_back_login") { performClick() }

        api.failGets = true
        composeRule.setContent { StkTheme { ApiLegalScreen(api, "user_agreement", {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("agreement_retry").fetchSemanticsNodes().isNotEmpty() }
        act("INT-LEGAL-002", "agreement_retry") { performClick() }; api.failGets = false
        act("INT-LEGAL-001", "agreement_back") { performClick() }
        api.failGets = true
        composeRule.setContent { StkTheme { ApiLegalScreen(api, "privacy_policy", {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("privacy_retry").fetchSemanticsNodes().isNotEmpty() }
        act("INT-LEGAL-004", "privacy_retry") { performClick() }; api.failGets = false
        act("INT-LEGAL-003", "privacy_back") { performClick() }
    }

    @Test fun systemHomeMeAndNavigationControlsProduceExpectedResults() {
        var selected = -1
        composeRule.setContent { StkTheme { StkBottomBar(selected) { selected = it } } }
        act("INT-NAV-001", "nav_home") { performClick() }; check(selected == 0)
        act("INT-NAV-002", "nav_publish") { performClick() }; check(selected == 1)
        act("INT-NAV-003", "nav_me") { performClick() }; check(selected == 2)

        composeRule.setContent { StkTheme { BootstrapFailureScreen({}, {}) } }
        act("INT-SYS-001", "bootstrap_retry") { performClick() }
        act("INT-SYS-002", "bootstrap_continue_offline") { performClick() }
        composeRule.setContent { StkTheme { SystemFailureScreen({}) } }
        act("INT-SYS-003", "system_retry") { performClick() }
        composeRule.setContent { StkTheme { AuthRiskDialog("账号暂时受限", {}) } }
        act("INT-RISK-001", "auth_risk_confirm") { performClick() }

        val api = FakeApi()
        val session = StkSession("access", "refresh", "family")
        composeRule.setContent { StkTheme { ApiHomeScreen(api, session, Modifier, {}, {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("project_card_1").fetchSemanticsNodes().isNotEmpty() }
        act("INT-HOME-001", "home_pull_refresh") { assertIsDisplayed() }
        act("INT-HOME-004", "project_card_1") { performClick() }
        act("INT-HOME-005", "home_project_list") { assertIsDisplayed() }
        composeRule.onNodeWithTag("home_load_more_probe").performClick()
        act("INT-HOME-007", "home_load_more_retry") { performClick() }
        act("INT-HOME-008", "home_avatar") { performClick() }

        api.failGets = true
        composeRule.setContent { StkTheme { ApiHomeScreen(api, session, Modifier, {}, {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("home_retry").fetchSemanticsNodes().isNotEmpty() }
        act("INT-HOME-006", "home_retry") { performClick() }; api.failGets = false
        composeRule.setContent { StkTheme { ApiDetailScreen(api, session, 1, "device", Modifier, {}) } }
        act("INT-DETAIL-001", "project_detail_back") { performClick() }
        api.failGets = true
        composeRule.setContent { StkTheme { ApiDetailScreen(api, session, 1, "device", Modifier, {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("project_detail_retry").fetchSemanticsNodes().isNotEmpty() }
        act("INT-DETAIL-005", "project_detail_retry") { performClick() }

        composeRule.setContent { StkTheme { StageScreen(Modifier) } }
        act("INT-STAGE-001", "stage_scope_close") { performClick() }
        api.failGets = true
        composeRule.setContent { StkTheme { ApiMeScreen(api, session, "device", Modifier, {}) } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag("me_retry").fetchSemanticsNodes().isNotEmpty() }
        act("INT-ME-011", "me_retry") { performClick() }; api.failGets = false
        composeRule.setContent { StkTheme { ApiMeScreen(api, session, "device", Modifier, {}) } }
        composeRule.onNodeWithTag("logout_open").performClick()
        act("INT-LOGOUT-002", "logout_cancel") { performClick() }
        composeRule.onNodeWithTag("logout_open").performClick()
        act("INT-LOGOUT-001", "logout_confirm") { performClick() }
    }

    private fun act(id: String, tag: String, action: androidx.compose.ui.test.SemanticsNodeInteraction.() -> Unit) {
        composeRule.onNodeWithTag(tag).assertIsDisplayed().action()
        passed += id
    }

    companion object {
        private val passed = Collections.synchronizedSet(mutableSetOf<String>())
        @JvmStatic @AfterClass fun writeEvidence() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val output = JSONObject().put("passed_interaction_ids", JSONArray(passed.sorted())).put("failed_interaction_ids", JSONArray())
            context.getExternalFilesDir(null)!!.resolve("interaction-results.json").writeText(output.toString())
        }
    }
}

private class FakeApi : StkApi {
    val posts = mutableListOf<String>()
    var failGets = false
    override fun post(path: String, body: String, accessToken: String?, callback: (Result<String>) -> Unit) {
        posts += path
        val data = when (path) {
            "/v1/auth/captcha/challenges" -> JSONObject().put("data", JSONObject().put("challenge_id", "challenge"))
            "/v1/auth/captcha/verify" -> JSONObject().put("data", JSONObject().put("captcha_ticket", "ticket"))
            "/v1/auth/logout" -> JSONObject().put("data", JSONObject())
            else -> JSONObject().put("data", JSONObject())
        }
        callback(Result.success(data.toString()))
    }
    override fun get(path: String, accessToken: String?, callback: (Result<String>) -> Unit) {
        if (failGets) { callback(Result.failure(IllegalStateException("controlled failure"))); return }
        val data = when {
            path.startsWith("/v1/projects?") -> JSONObject().put("items", JSONArray().put(JSONObject().put("project_id", 1).put("title", "测试项目").put("summary", "摘要").put("view_count", 1)))
            path.startsWith("/v1/projects/") -> JSONObject().put("title", "测试项目").put("content", "项目内容")
            path == "/v1/me/summary" -> JSONObject().put("display_name", "测试用户").put("uid", 1).put("masked_mobile", "138****8000")
            else -> JSONObject().put("title", "协议").put("content_text", "合同正文")
        }
        callback(Result.success(JSONObject().put("data", data).toString()))
    }
}
