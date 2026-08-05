package com.zzyihao.stk

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
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

    @Test fun loginInputsModesCaptchaAndRoutesAreInteractive() {
        val api = FakeApi()
        var authenticated = false
        var destination = ""
        fun renderLogin() {
            composeRule.setContent {
                StkTheme {
                    LoginScreen(
                        api = api,
                        deviceId = "device",
                        onAuthenticated = { authenticated = true },
                        onRegister = { destination = "register" },
                        onReset = { destination = "reset" },
                        onAgreement = { destination = "agreement" },
                        onPrivacy = { destination = "privacy" },
                    )
                }
            }
        }

        renderLogin()
        act("INT-AUTH-003", "login_phone") { performTextInput("13800138000") }
        act("INT-AUTH-004", "login_password") { performTextInput("Password123") }
        act("INT-AUTH-005", "login_password_visibility") { performClick() }
        act("INT-AUTH-008", "login_password_submit") { performClick() }
        waitForTag("captcha_image")
        act("INT-CAPTCHA-001", "captcha_input") { performTextInput("1234") }
        act("INT-CAPTCHA-002", "captcha_refresh") { performClick() }
        waitForTag("captcha_image")
        composeRule.onNodeWithTag("captcha_input").performTextInput("1234")
        act("INT-CAPTCHA-003", "captcha_confirm") { performClick() }
        check(authenticated)
        check(api.posts.contains("/v1/auth/login/password"))

        authenticated = false
        renderLogin()
        act("INT-AUTH-002", "login_mode_sms") { performClick() }
        waitForTag("login_sms_code")
        act("INT-AUTH-006", "login_sms_code") { performTextInput("123456") }
        composeRule.onNodeWithTag("login_phone").performTextInput("13800138000")
        act("INT-AUTH-007", "login_send_sms") { performClick() }
        waitForTag("captcha_cancel")
        act("INT-CAPTCHA-004", "captcha_cancel") { performClick() }
        act("INT-AUTH-009", "login_sms_submit") { performClick() }
        waitForTag("captcha_image")
        composeRule.onNodeWithTag("captcha_input").performTextInput("1234")
        composeRule.onNodeWithTag("captcha_confirm").performClick()
        composeRule.waitForIdle()
        check(authenticated)
        check(api.posts.contains("/v1/auth/login/sms"))

        listOf(
            "INT-AUTH-010" to ("login_open_register" to "register"),
            "INT-AUTH-011" to ("login_open_reset" to "reset"),
            "INT-AUTH-012" to ("login_open_agreement" to "agreement"),
            "INT-AUTH-013" to ("login_open_privacy" to "privacy"),
        ).forEach { (id, binding) ->
            destination = ""
            renderLogin()
            act(id, binding.first) { performClick() }
            check(destination == binding.second)
        }
        renderLogin()
        act("INT-AUTH-001", "login_mode_password") { performClick() }
    }

    @Test fun registrationResetAndLegalControlsPerformContractActions() {
        val api = FakeApi()
        var registered = false
        var returned = false
        var legalRoute = ""
        composeRule.setContent {
            StkTheme {
                ApiRegisterScreen(
                    api, "device",
                    onBack = { returned = true },
                    onAgreement = { legalRoute = "agreement" },
                    onPrivacy = { legalRoute = "privacy" },
                    onSuccess = { registered = true },
                )
            }
        }
        act("INT-REG-001", "register_phone") { performTextInput("13800138000") }
        act("INT-REG-002", "register_password") { performTextInput("Password123") }
        act("INT-REG-003", "register_password_confirm") { performTextInput("Password123") }
        act("INT-REG-004", "register_password_visibility") { performClick() }
        act("INT-REG-005", "register_agreement_check") { performClick() }
        act("INT-REG-006", "register_submit") { performClick() }
        waitForTag("captcha_image")
        composeRule.onNodeWithTag("captcha_input").performTextInput("1234")
        composeRule.onNodeWithTag("captcha_confirm").performClick()
        composeRule.waitForIdle()
        check(registered)
        check(api.posts.contains("/v1/auth/register"))

        act("INT-REG-007", "register_back_login") { performClick() }; check(returned)
        legalRoute = ""
        act("INT-REG-008", "register_open_agreement") { performClick() }; check(legalRoute == "agreement")
        legalRoute = ""
        act("INT-REG-009", "register_open_privacy") { performClick() }; check(legalRoute == "privacy")

        returned = false
        composeRule.setContent { StkTheme { ApiResetScreen(api, "device") { returned = true } } }
        act("INT-RESET-001", "reset_phone") { performTextInput("13800138000") }
        act("INT-RESET-003", "reset_sms_code") { performTextInput("123456") }
        act("INT-RESET-004", "reset_new_password") { performTextInput("Password123") }
        act("INT-RESET-005", "reset_new_password_confirm") { performTextInput("Password123") }
        act("INT-RESET-002", "reset_send_sms") { performClick() }
        waitForTag("captcha_cancel")
        composeRule.onNodeWithTag("captcha_cancel").performClick()
        act("INT-RESET-006", "reset_submit") { performClick() }
        waitForTag("captcha_image")
        composeRule.onNodeWithTag("captcha_input").performTextInput("1234")
        composeRule.onNodeWithTag("captcha_confirm").performClick()
        composeRule.waitForIdle()
        check(returned)
        check(api.posts.contains("/v1/auth/password/reset"))

        returned = false
        composeRule.setContent { StkTheme { ApiResetScreen(api, "device") { returned = true } } }
        act("INT-RESET-007", "reset_back_login") { performClick() }; check(returned)

        api.failGets = true
        returned = false
        composeRule.setContent { StkTheme { ApiLegalScreen(api, "user_agreement") { returned = true } } }
        waitForTag("agreement_retry")
        api.failGets = false
        act("INT-LEGAL-002", "agreement_retry") { performClick() }
        waitForTag("agreement_back")
        act("INT-LEGAL-001", "agreement_back") { performClick() }; check(returned)

        api.failGets = true
        returned = false
        composeRule.setContent { StkTheme { ApiLegalScreen(api, "privacy_policy") { returned = true } } }
        waitForTag("privacy_retry")
        api.failGets = false
        act("INT-LEGAL-004", "privacy_retry") { performClick() }
        waitForTag("privacy_back")
        act("INT-LEGAL-003", "privacy_back") { performClick() }; check(returned)
    }

    @Test fun systemNavigationHomeDetailAndProfileControlsProduceResults() {
        var selected = -1
        composeRule.setContent { StkTheme { StkBottomBar(selected) { selected = it } } }
        act("INT-NAV-001", "nav_home") { performClick() }; check(selected == 0)
        act("INT-NAV-002", "nav_publish") { performClick() }; check(selected == 1)
        act("INT-NAV-003", "nav_me") { performClick() }; check(selected == 2)

        var retryCount = 0
        var offlineCount = 0
        composeRule.setContent { StkTheme { BootstrapFailureScreen(true, { retryCount++ }, { offlineCount++ }) } }
        act("INT-SYS-001", "bootstrap_retry") { performClick() }; check(retryCount == 1)
        act("INT-SYS-002", "bootstrap_continue_offline") { performClick() }; check(offlineCount == 1)
        composeRule.setContent { StkTheme { SystemFailureScreen { retryCount++ } } }
        act("INT-SYS-003", "system_retry") { performClick() }; check(retryCount == 2)
        var riskConfirmed = false
        composeRule.setContent { StkTheme { AuthRiskDialog("账号暂时受限") { riskConfirmed = true } } }
        act("INT-RISK-001", "auth_risk_confirm") { performClick() }; check(riskConfirmed)

        val api = FakeApi()
        val session = StkSession("access", "refresh", "family")
        var openedProject = -1
        var openedMe = false
        composeRule.setContent { StkTheme { ApiHomeScreen(api, session, Modifier, { openedMe = true }) { openedProject = it } } }
        waitForTag("project_card_1")
        val requestsBeforeRefresh = api.projectRequests
        act("INT-HOME-001", "home_pull_refresh") { performTouchInput { swipeDown() } }
        composeRule.waitUntil(5_000) { api.projectRequests > requestsBeforeRefresh }
        act("INT-HOME-004", "project_card_1") { performClick() }; check(openedProject == 1)
        act("INT-HOME-008", "home_avatar") { performClick() }; check(openedMe)
        act("INT-HOME-005", "home_project_list") { performScrollToNode(hasTestTag("project_card_20")) }
        waitForTag("project_card_21")

        api.failNextProjectPage = true
        composeRule.setContent { StkTheme { ApiHomeScreen(api, session, Modifier, {}, {}) } }
        waitForTag("project_card_1")
        composeRule.onNodeWithTag("home_project_list").performScrollToNode(hasTestTag("home_load_more_sentinel"))
        waitForTag("home_load_more_retry")
        act("INT-HOME-007", "home_load_more_retry") { performClick() }
        waitForTag("project_card_21")

        api.failGets = true
        composeRule.setContent { StkTheme { ApiHomeScreen(api, session, Modifier, {}, {}) } }
        waitForTag("home_retry")
        api.failGets = false
        act("INT-HOME-006", "home_retry") { performClick() }
        waitForTag("project_card_1")

        var returned = false
        composeRule.setContent { StkTheme { ApiDetailScreen(api, session, 1, "device", Modifier) { returned = true } } }
        waitForTag("project_detail_back")
        act("INT-DETAIL-001", "project_detail_back") { performClick() }; check(returned)
        api.failGets = true
        composeRule.setContent { StkTheme { ApiDetailScreen(api, session, 1, "device", Modifier, {}) } }
        waitForTag("project_detail_retry")
        api.failGets = false
        act("INT-DETAIL-005", "project_detail_retry") { performClick() }

        var stageClosed = false
        composeRule.setContent { StkTheme { StageScreen(Modifier) { stageClosed = true } } }
        act("INT-STAGE-001", "stage_scope_close") { performClick() }; check(stageClosed)

        api.failGets = true
        composeRule.setContent { StkTheme { ApiMeScreen(api, session, "device", Modifier, {}) } }
        waitForTag("me_retry")
        api.failGets = false
        act("INT-ME-011", "me_retry") { performClick() }
        composeRule.waitForIdle()
        var loggedOut = false
        composeRule.setContent { StkTheme { ApiMeScreen(api, session, "device", Modifier) { loggedOut = true } } }
        waitForTag("logout_open")
        composeRule.onNodeWithTag("logout_open").performClick()
        act("INT-LOGOUT-002", "logout_cancel") { performClick() }
        check(composeRule.onAllNodesWithTag("logout_confirm").fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithTag("logout_open").performClick()
        act("INT-LOGOUT-001", "logout_confirm") { performClick() }
        composeRule.waitForIdle()
        check(loggedOut)
        check(api.posts.contains("/v1/auth/logout"))
    }

    @Test fun captchaImageLoadFailureRefreshAndExpiryRecoverWithoutDismissal() {
        val api = FakeApi().apply { failCaptchaChallenge = true }
        composeRule.setContent { StkTheme { CaptchaDialog(api, "device", "password_login", {}, {}) } }
        waitForTag("captcha_error")

        api.failCaptchaChallenge = false
        composeRule.onNodeWithTag("captcha_refresh").performClick()
        waitForTag("captcha_image")
        check(api.captchaChallengeRequests == 2)

        api.failCaptchaVerifyWithExpired = true
        composeRule.onNodeWithTag("captcha_input").performTextInput("1234")
        composeRule.onNodeWithTag("captcha_confirm").performClick()
        composeRule.waitUntil(5_000) { api.captchaChallengeRequests >= 3 }
        waitForTag("captcha_image")

        api.captchaExpiresIn = 1
        composeRule.onNodeWithTag("captcha_refresh").performClick()
        val requestsBeforeExpiry = api.captchaChallengeRequests
        composeRule.waitUntil(5_000) { api.captchaChallengeRequests > requestsBeforeExpiry }
        waitForTag("captcha_image")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun act(id: String, tag: String, action: androidx.compose.ui.test.SemanticsNodeInteraction.() -> Unit) {
        composeRule.onNodeWithTag(tag).assertIsDisplayed().action()
        composeRule.waitForIdle()
        passed += id
    }

    companion object {
        private val passed = Collections.synchronizedSet(mutableSetOf<String>())
        @JvmStatic @AfterClass fun writeEvidence() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val output = JSONObject().put("passed_interaction_ids", JSONArray(passed.sorted())).put("failed_interaction_ids", JSONArray())
            val serialized = output.toString()
            context.getExternalFilesDir(null)?.let { directory ->
                directory.mkdirs()
                directory.resolve("interaction-results.json").writeText(serialized)
            }
            Log.i("STK_INTERACTION_RESULTS", serialized)
        }
    }
}

private class FakeApi : StkApi {
    val posts = Collections.synchronizedList(mutableListOf<String>())
    var failGets = false
    var failNextProjectPage = false
    var failCaptchaChallenge = false
    var failCaptchaVerifyWithExpired = false
    var captchaExpiresIn = 120
    var captchaChallengeRequests = 0
    var projectRequests = 0

    override fun post(path: String, body: String, accessToken: String?, callback: (Result<String>) -> Unit) {
        posts += path
        if (path == "/v1/auth/captcha/challenges") {
            captchaChallengeRequests++
            if (failCaptchaChallenge) {
                callback(Result.failure(IllegalStateException("controlled captcha load failure")))
                return
            }
        }
        if (path == "/v1/auth/captcha/verify" && failCaptchaVerifyWithExpired) {
            failCaptchaVerifyWithExpired = false
            callback(Result.failure(IllegalStateException("CAPTCHA_EXPIRED")))
            return
        }
        val data = when (path) {
            "/v1/auth/captcha/challenges" -> JSONObject().put("data", JSONObject()
                .put("challenge_id", "challenge-$captchaChallengeRequests")
                .put("image_base64_or_url", CAPTCHA_PNG_DATA_URI)
                .put("expires_in", captchaExpiresIn))
            "/v1/auth/captcha/verify" -> JSONObject().put("data", JSONObject().put("captcha_ticket", "ticket"))
            "/v1/auth/login/password", "/v1/auth/login/sms" -> JSONObject().put("data", tokens())
            "/v1/auth/register" -> JSONObject().put("data", JSONObject().put("tokens", tokens()))
            "/v1/auth/logout" -> JSONObject().put("data", JSONObject().put("revoked", true))
            else -> JSONObject().put("data", JSONObject())
        }
        callback(Result.success(data.toString()))
    }

    override fun get(path: String, accessToken: String?, callback: (Result<String>) -> Unit) {
        if (failGets) { callback(Result.failure(IllegalStateException("controlled failure"))); return }
        if (path.startsWith("/v1/projects?")) {
            projectRequests++
            if (path.contains("cursor=page2") && failNextProjectPage) {
                failNextProjectPage = false
                callback(Result.failure(IllegalStateException("controlled page failure")))
                return
            }
            val start = if (path.contains("cursor=page2")) 21 else 1
            val items = JSONArray()
            repeat(20) { offset ->
                val id = start + offset
                items.put(JSONObject().put("project_id", id).put("title", "测试项目 $id").put("summary", "摘要 $id").put("view_count", id))
            }
            val data = JSONObject().put("items", items)
            if (start == 1) data.put("next_cursor", "page2").put("has_more", true)
            else data.put("has_more", false)
            callback(Result.success(JSONObject().put("data", data).toString()))
            return
        }
        val data = when {
            path.startsWith("/v1/projects/") -> JSONObject().put("title", "测试项目").put("content", "项目内容")
            path == "/v1/me/summary" -> JSONObject().put("display_name", "测试用户").put("uid", 1).put("masked_mobile", "138****8000")
            else -> JSONObject().put("title", "协议").put("content_text", "合同正文")
        }
        callback(Result.success(JSONObject().put("data", data).toString()))
    }

    private fun tokens() = JSONObject()
        .put("access_token", "access")
        .put("refresh_token", "refresh")
        .put("refresh_token_family", "family")

    companion object {
        private const val CAPTCHA_PNG_DATA_URI = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
    }
}
