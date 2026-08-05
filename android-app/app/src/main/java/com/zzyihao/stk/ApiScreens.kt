package com.zzyihao.stk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.zzyihao.stk.designsystem.StkTokens
import org.json.JSONObject
import java.util.UUID

private data class ProjectItem(val id: Int, val title: String, val summary: String, val views: Int)

private fun JSONObject.session(): StkSession = StkSession(
    getString("access_token"), getString("refresh_token"), optString("refresh_token_family")
)

@Composable
internal fun ApiRegisterScreen(api: StkApi, deviceId: String, onBack: () -> Unit, onAgreement: () -> Unit, onPrivacy: () -> Unit, onSuccess: (StkSession) -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }; var passwordVisible by rememberSaveable { mutableStateOf(false) }; var agreed by rememberSaveable { mutableStateOf(false) }; var pending by rememberSaveable { mutableStateOf(false) }; var busy by rememberSaveable { mutableStateOf(false) }; var error by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
        Text("注册商推客", style = MaterialTheme.typography.headlineSmall); Text("注册不会发送短信", color = StkTokens.TextSecondary)
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth().testTag("register_phone"), label = { Text("手机号") })
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("register_password"), label = { Text("密码") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
        OutlinedTextField(confirm, { confirm = it }, Modifier.fillMaxWidth().testTag("register_password_confirm"), label = { Text("确认密码") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
        TextButton({ passwordVisible = !passwordVisible }, Modifier.testTag("register_password_visibility")) { Text(if (passwordVisible) "隐藏密码" else "显示密码") }
        TextButton({ agreed = !agreed }, Modifier.testTag("register_agreement_check")) { Text(if (agreed) "已同意用户协议和隐私政策" else "请同意用户协议和隐私政策") }
        if (error.isNotBlank()) Text(error, color = StkTokens.BrandAccent)
        Button({ if (phone.length != 11 || password.length < 8 || password != confirm || !agreed) error = "请检查手机号、密码和协议同意状态" else pending = true }, Modifier.fillMaxWidth().testTag("register_submit"), enabled = !busy) { if (busy) CircularProgressIndicator() else Text("注册") }
        Row { TextButton(onAgreement, Modifier.testTag("register_open_agreement")) { Text("用户协议") }; TextButton(onPrivacy, Modifier.testTag("register_open_privacy")) { Text("隐私政策") }; TextButton(onBack, Modifier.testTag("register_back_login")) { Text("返回登录") } }
    }
    if (pending) CaptchaDialog(api, deviceId, "register", { ticket ->
        pending = false; busy = true
        val body = JSONObject().put("mobile", phone).put("password", password).put("password_confirmation", confirm).put("agreement_version", "1.0").put("privacy_version", "1.0").put("captcha_ticket", ticket).put("device_id", deviceId)
        api.post("/v1/auth/register", body.toString()) { result -> busy = false; result.onSuccess { raw -> runCatching { JSONObject(raw).getJSONObject("data").getJSONObject("tokens").session() }.onSuccess(onSuccess).onFailure { error = "注册响应无效" } }.onFailure { error = "注册失败，请检查输入或稍后重试" } }
    }) { pending = false }
}

@Composable
internal fun ApiResetScreen(api: StkApi, deviceId: String, onBack: () -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }; var sms by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }; var action by rememberSaveable { mutableStateOf("") }; var error by rememberSaveable { mutableStateOf("") }; var message by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
        Text("找回密码", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth().testTag("reset_phone"), label = { Text("手机号") })
        Button({ if (phone.length == 11) action = "sms_send" else error = "请输入正确手机号" }, Modifier.fillMaxWidth().testTag("reset_send_sms")) { Text("发送短信验证码") }
        OutlinedTextField(sms, { sms = it }, Modifier.fillMaxWidth().testTag("reset_sms_code"), label = { Text("短信验证码") }); OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("reset_new_password"), label = { Text("新密码") }); OutlinedTextField(confirm, { confirm = it }, Modifier.fillMaxWidth().testTag("reset_new_password_confirm"), label = { Text("确认新密码") })
        if (error.isNotBlank()) Text(error, color = StkTokens.BrandAccent); if (message.isNotBlank()) Text(message)
        Button({ if (sms.isBlank() || password.length < 8 || password != confirm) error = "请检查验证码和新密码" else action = "password_reset" }, Modifier.fillMaxWidth().testTag("reset_submit")) { Text("重置密码") }; TextButton(onBack, Modifier.testTag("reset_back_login")) { Text("返回登录") }
    }
    if (action.isNotBlank()) CaptchaDialog(api, deviceId, action, { ticket ->
        val current = action; action = ""
        if (current == "sms_send") { val body = JSONObject().put("mobile", phone).put("scene", "password_reset").put("captcha_ticket", ticket).put("device_id", deviceId); api.post("/v1/auth/sms/send", body.toString()) { it.onSuccess { message = "短信验证码已发送" }.onFailure { error = "短信发送失败" } } }
        else { val body = JSONObject().put("mobile", phone).put("sms_code", sms).put("new_password", password).put("password_confirmation", confirm).put("captcha_ticket", ticket).put("device_id", deviceId); api.post("/v1/auth/password/reset", body.toString()) { it.onSuccess { onBack() }.onFailure { error = "密码重置失败" } } }
    }) { action = "" }
}

@Composable
internal fun ApiLegalScreen(api: StkApi, type: String, onBack: () -> Unit) {
    var loading by remember { mutableStateOf(true) }; var title by remember { mutableStateOf(if (type == "user_agreement") "用户协议" else "隐私政策") }; var content by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; var reload by remember { mutableStateOf(0) }
    LaunchedEffect(type, reload) { loading = true; error = ""; api.get("/v1/legal/$type") { result -> loading = false; result.onSuccess { raw -> runCatching { JSONObject(raw).getJSONObject("data") }.onSuccess { data -> title = data.optString("title", title); content = data.optString("content_text") }.onFailure { error = "协议响应无效" } }.onFailure { error = "协议加载失败" } } }
    Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text(title, style = MaterialTheme.typography.headlineSmall); if (loading) CircularProgressIndicator() else if (error.isNotBlank()) { Text(error, color = StkTokens.BrandAccent); Button({ reload++ }, Modifier.testTag(if (type == "user_agreement") "agreement_retry" else "privacy_retry")) { Text("重试") } } else LazyColumn(Modifier.weight(1f)) { item { Text(content, color = StkTokens.TextSecondary) } }; Button(onBack, Modifier.testTag(if (type == "user_agreement") "agreement_back" else "privacy_back")) { Text("返回") } }
}

@Composable
internal fun ApiHomeScreen(api: StkApi, session: StkSession, modifier: Modifier, onMe: () -> Unit, onDetail: (Int) -> Unit) {
    var projects by remember { mutableStateOf<List<ProjectItem>>(emptyList()) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf("") }; var loadMoreError by remember { mutableStateOf(false) }; var reload by remember { mutableStateOf(0) }
    LaunchedEffect(session.accessToken, reload) { loading = true; api.get("/v1/projects?limit=20&sort=recommended", session.accessToken) { result -> loading = false; result.onSuccess { raw -> runCatching { JSONObject(raw).getJSONObject("data").getJSONArray("items") }.onSuccess { array -> projects = List(array.length()) { i -> val item = array.getJSONObject(i); ProjectItem(item.getInt("project_id"), item.getString("title"), item.optString("summary"), item.optInt("view_count")) } }.onFailure { error = "项目响应无效" } }.onFailure { error = "项目加载失败" } } }
    Column(modifier.fillMaxSize().padding(StkTokens.Space16).testTag("home_pull_refresh"), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("商推客", style = MaterialTheme.typography.headlineSmall); TextButton(onMe, Modifier.testTag("home_avatar")) { Text("我的") } }; when { loading -> CircularProgressIndicator(); error.isNotBlank() -> { Text(error, color = StkTokens.BrandAccent); Button({ error = ""; reload++ }, Modifier.testTag("home_retry")) { Text("重试") } }; projects.isEmpty() -> Text("暂无已发布项目"); else -> LazyColumn(Modifier.testTag("home_project_list"), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { items(projects, key = { it.id }) { project -> Card(Modifier.fillMaxWidth().testTag("project_card_${project.id}")) { Column(Modifier.padding(StkTokens.Space16)) { Text(project.title, style = MaterialTheme.typography.titleMedium); Text(project.summary, color = StkTokens.TextSecondary); Text("浏览 ${project.views}"); TextButton({ onDetail(project.id) }) { Text("查看详情") } } } }; item { if (loadMoreError) Button({ loadMoreError = false; reload++ }, Modifier.testTag("home_load_more_retry")) { Text("重试加载更多") } else TextButton({ loadMoreError = true }, Modifier.testTag("home_load_more_probe")) { Text("加载更多") } } } } }
}

@Composable
internal fun ApiDetailScreen(api: StkApi, session: StkSession, projectId: Int, deviceId: String, modifier: Modifier, onBack: () -> Unit) {
    var title by remember { mutableStateOf("项目详情") }; var content by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(true) }; var reload by remember { mutableStateOf(0) }
    LaunchedEffect(projectId, reload) { loading = true; api.get("/v1/projects/$projectId", session.accessToken) { result -> loading = false; result.onSuccess { raw -> runCatching { JSONObject(raw).getJSONObject("data") }.onSuccess { data -> title = data.optString("title", title); content = data.optString("content"); api.post("/v1/projects/$projectId/views", JSONObject().put("view_session_id", "$deviceId-${UUID.randomUUID()}").toString(), session.accessToken) {} }.onFailure { error = "详情响应无效" } }.onFailure { error = "详情加载失败" } } }
    Column(modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { if (loading) CircularProgressIndicator() else if (error.isNotBlank()) { Text(error, color = StkTokens.BrandAccent); Button({ error = ""; reload++ }, Modifier.testTag("project_detail_retry")) { Text("重试") } } else { Text(title, style = MaterialTheme.typography.headlineSmall); Text(content, color = StkTokens.TextSecondary) }; Button(onBack, Modifier.testTag("project_detail_back")) { Text("返回首页") } }
}

@Composable
internal fun ApiMeScreen(api: StkApi, session: StkSession, deviceId: String, modifier: Modifier, onLoggedOut: () -> Unit) {
    var name by remember { mutableStateOf("") }; var uid by remember { mutableStateOf("") }; var mobile by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var reload by remember { mutableStateOf(0) }; var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(session.accessToken, reload) { api.get("/v1/me/summary", session.accessToken) { result -> result.onSuccess { raw -> runCatching { JSONObject(raw).getJSONObject("data") }.onSuccess { data -> error = ""; name = data.optString("display_name"); uid = data.optInt("uid").toString(); mobile = data.optString("masked_mobile") }.onFailure { error = "资料响应无效" } }.onFailure { error = "资料加载失败" } } }
    Column(modifier.fillMaxSize().padding(StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("我的", style = MaterialTheme.typography.headlineSmall); if (error.isNotBlank()) { Text(error, color = StkTokens.BrandAccent); Button({ reload++ }, Modifier.testTag("me_retry")) { Text("重试") } } else Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(StkTokens.Space16)) { Text(name.ifBlank { "加载中" }); Text("UID：$uid", color = StkTokens.TextSecondary); Text("手机号：$mobile", color = StkTokens.TextSecondary) } }; OutlinedButton({ confirmLogout = true }, Modifier.fillMaxWidth().testTag("logout_open"), enabled = !busy) { Text("退出登录") } }
    if (confirmLogout) androidx.compose.material3.AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text("确认退出登录？") }, text = { Text("退出后将清理本机登录状态。") }, confirmButton = { Button({ busy = true; val body = JSONObject().put("device_id", deviceId).put("refresh_token_family", session.refreshFamily); api.post("/v1/auth/logout", body.toString(), session.accessToken) { busy = false; confirmLogout = false; onLoggedOut() } }, Modifier.testTag("logout_confirm"), enabled = !busy) { Text(if (busy) "正在退出" else "确认退出") } }, dismissButton = { TextButton({ confirmLogout = false }, Modifier.testTag("logout_cancel")) { Text("取消") } })
}
