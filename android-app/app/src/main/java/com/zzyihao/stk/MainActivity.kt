package com.zzyihao.stk

import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import androidx.compose.material3.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.zzyihao.stk.designsystem.StkTokens
import com.zzyihao.stk.designsystem.StkTheme
import org.json.JSONObject
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StkTheme { StkApp() } }
    }
}

private enum class Route { LOGIN, REGISTER, RESET, LEGAL_AGREEMENT, LEGAL_PRIVACY, HOME, PUBLISH_STAGE, ME, DETAIL }

@Composable
private fun StkApp() {
    val context = LocalContext.current
    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown-device" }
    var session by remember { mutableStateOf(StkSessionStore.read(context)) }
    val api = remember(deviceId) {
        StkApiClient(defaultHeaders = mapOf(
            "app_version" to BuildConfig.VERSION_NAME,
            "version_code" to BuildConfig.VERSION_CODE.toString(),
            "device_id" to deviceId,
            "locale" to Locale.getDefault().toLanguageTag(),
        ))
    }
    var bootstrapResolved by rememberSaveable { mutableStateOf(false) }
    var route by rememberSaveable { mutableStateOf(if (session == null) Route.LOGIN else Route.HOME) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var selectedProjectId by rememberSaveable { mutableStateOf(0) }
    var legalReturnRoute by rememberSaveable { mutableStateOf(Route.LOGIN) }
    if (!bootstrapResolved) {
        BootstrapGate(api, session?.accessToken, session != null) { authenticated ->
            if (!authenticated && session != null) {
                session = null
                StkSessionStore.clear(context)
            }
            route = if (authenticated) Route.HOME else Route.LOGIN
            bootstrapResolved = true
        }
        return
    }
    val authenticated = route == Route.HOME || route == Route.PUBLISH_STAGE || route == Route.ME || route == Route.DETAIL
    if (!authenticated) {
        when (route) {
            Route.LOGIN -> LoginScreen(api, deviceId, { session = it; StkSessionStore.save(context, it); route = Route.HOME }, { route = Route.REGISTER }, { route = Route.RESET }, { legalReturnRoute = Route.LOGIN; route = Route.LEGAL_AGREEMENT }, { legalReturnRoute = Route.LOGIN; route = Route.LEGAL_PRIVACY })
            Route.REGISTER -> ApiRegisterScreen(api, deviceId, { route = Route.LOGIN }, { legalReturnRoute = Route.REGISTER; route = Route.LEGAL_AGREEMENT }, { legalReturnRoute = Route.REGISTER; route = Route.LEGAL_PRIVACY }) { newSession -> session = newSession; StkSessionStore.save(context, newSession); route = Route.HOME }
            Route.RESET -> ApiResetScreen(api, deviceId) { route = Route.LOGIN }
            Route.LEGAL_AGREEMENT -> ApiLegalScreen(api, "user_agreement") { route = legalReturnRoute }
            Route.LEGAL_PRIVACY -> ApiLegalScreen(api, "privacy_policy") { route = legalReturnRoute }
            else -> Unit
        }
        return
    }
    Scaffold(bottomBar = {
        StkBottomBar(tab) { index -> tab = index; route = when (index) { 0 -> Route.HOME; 1 -> Route.PUBLISH_STAGE; else -> Route.ME } }
    }) { padding ->
        when (route) {
            Route.HOME -> ApiHomeScreen(api, session!!, Modifier.padding(padding), { tab = 2; route = Route.ME }) { projectId -> selectedProjectId = projectId; route = Route.DETAIL }
            Route.PUBLISH_STAGE -> StageScreen(Modifier.padding(padding)) { tab = 0; route = Route.HOME }
            Route.ME -> ApiMeScreen(api, session!!, deviceId, Modifier.padding(padding)) { session = null; StkSessionStore.clear(context); route = Route.LOGIN }
            Route.DETAIL -> ApiDetailScreen(api, session!!, selectedProjectId, deviceId, Modifier.padding(padding)) { route = Route.HOME }
            else -> Unit
        }
    }
}

@Composable
internal fun StkBottomBar(selected: Int, onSelected: (Int) -> Unit) {
    NavigationBar {
        listOf("首页", "发布", "我的").forEachIndexed { index, label ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelected(index) },
                icon = { Text(when (index) { 0 -> "⌂"; 1 -> "+"; else -> "我" }) },
                label = { Text(label) },
                modifier = Modifier.testTag(listOf("nav_home", "nav_publish", "nav_me")[index]),
            )
        }
    }
}

@Composable
internal fun LoginScreen(api: StkApi, deviceId: String, onAuthenticated: (StkSession) -> Unit, onRegister: () -> Unit, onReset: () -> Unit, onAgreement: () -> Unit, onPrivacy: () -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var smsCode by rememberSaveable { mutableStateOf("") }
    var passwordMode by rememberSaveable { mutableStateOf(true) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }
    var pendingAction by rememberSaveable { mutableStateOf("") }
    var submitting by rememberSaveable { mutableStateOf(false) }
    fun authenticate(ticket: String, sms: Boolean) {
        val body = JSONObject().put("mobile", phone).put("captcha_ticket", ticket).put("device_id", deviceId).put("device_name", "Android")
        if (sms) body.put("sms_code", smsCode) else body.put("password", password)
        api.post(if (sms) "/v1/auth/login/sms" else "/v1/auth/login/password", body.toString()) { result ->
            submitting = false
            result.onSuccess { raw ->
                runCatching {
                    val data = JSONObject(raw).getJSONObject("data")
                    StkSession(data.getString("access_token"), data.getString("refresh_token"), data.optString("refresh_token_family"))
                }.onSuccess(onAuthenticated).onFailure { error = "登录响应无效，请重试" }
            }.onFailure { error = "登录失败，请检查网络或输入" }
        }
    }
    Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
        Text("登录商推客", style = MaterialTheme.typography.headlineSmall)
        Text("使用手机号安全登录", color = StkTokens.TextSecondary)
        Row { TextButton(onClick = { passwordMode = true }, Modifier.testTag("login_mode_password")) { Text("密码登录") }; TextButton(onClick = { passwordMode = false }, Modifier.testTag("login_mode_sms")) { Text("短信登录") } }
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth().testTag("login_phone"), label = { Text("手机号") })
        if (passwordMode) {
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("login_password"), label = { Text("登录密码") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
            TextButton(onClick = { passwordVisible = !passwordVisible }, Modifier.testTag("login_password_visibility")) { Text(if (passwordVisible) "隐藏密码" else "显示密码") }
        } else {
            OutlinedTextField(smsCode, { smsCode = it }, Modifier.fillMaxWidth().testTag("login_sms_code"), label = { Text("短信验证码") })
            TextButton(onClick = { if (phone.length == 11) pendingAction = "sms_send" else error = "请输入正确手机号" }, Modifier.testTag("login_send_sms")) { Text("发送短信验证码") }
        }
        if (error.isNotBlank()) Text(error, color = StkTokens.BrandAccent)
        Button(onClick = { if (phone.length != 11 || (passwordMode && password.isBlank()) || (!passwordMode && smsCode.isBlank())) error = "请完整填写登录信息" else pendingAction = if (passwordMode) "password_login" else "sms_login" }, Modifier.fillMaxWidth().testTag(if (passwordMode) "login_password_submit" else "login_sms_submit")) { if (submitting) CircularProgressIndicator() else Text(if (passwordMode) "密码登录" else "短信登录") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = onRegister, Modifier.testTag("login_open_register")) { Text("注册") }; TextButton(onClick = onReset, Modifier.testTag("login_open_reset")) { Text("找回密码") } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { TextButton(onClick = onAgreement, Modifier.testTag("login_open_agreement")) { Text("用户协议") }; TextButton(onClick = onPrivacy, Modifier.testTag("login_open_privacy")) { Text("隐私政策") } }
    }
    if (pendingAction.isNotBlank()) CaptchaDialog(api, deviceId, pendingAction, { ticket ->
        val action = pendingAction
        pendingAction = ""
        if (action == "sms_send") {
            api.post("/v1/auth/sms/send", JSONObject().put("mobile", phone).put("scene", "login").put("captcha_ticket", ticket).put("device_id", deviceId).toString()) { result -> result.onFailure { error = "短信发送失败" } }
        } else { submitting = true; authenticate(ticket, action == "sms_login") }
    }) { pendingAction = "" }
}

@Composable
internal fun CaptchaDialog(api: StkApi, deviceId: String, action: String, onVerified: (String) -> Unit, onDismiss: () -> Unit) {
    var challengeId by remember(action) { mutableStateOf("") }
    var imageDataUri by remember(action) { mutableStateOf("") }
    var answer by remember(action) { mutableStateOf("") }
    var error by remember(action) { mutableStateOf("") }
    var loading by remember(action) { mutableStateOf(false) }
    var verifying by remember(action) { mutableStateOf(false) }
    var expiresAt by remember(action) { mutableStateOf(0L) }
    val captchaImage = remember(imageDataUri) { decodeCaptchaDataUri(imageDataUri) }
    fun load() {
        if (loading || verifying) return
        loading = true
        error = ""
        answer = ""
        imageDataUri = ""
        api.post("/v1/auth/captcha/challenges", JSONObject().put("action", action).put("device_id", deviceId).toString()) { result ->
            loading = false
            result.onSuccess { raw ->
                runCatching {
                    val data = JSONObject(raw).getJSONObject("data")
                    Triple(data.getString("challenge_id"), data.getString("image_base64_or_url"), data.optInt("expires_in", 120))
                }.onSuccess { (id, image, expiresIn) ->
                    if (decodeCaptchaDataUri(image) == null) {
                        challengeId = ""
                        error = "验证码图片无效，请刷新"
                    } else {
                        challengeId = id
                        imageDataUri = image
                        expiresAt = System.currentTimeMillis() + expiresIn.coerceAtLeast(1) * 1_000L
                    }
                }.onFailure { error = "验证码响应无效，请刷新" }
            }.onFailure { error = "验证码加载失败，请重试" }
        }
    }
    LaunchedEffect(action) { load() }
    LaunchedEffect(expiresAt) {
        if (expiresAt > 0L) {
            delay((expiresAt - System.currentTimeMillis()).coerceAtLeast(1L))
            if (expiresAt <= System.currentTimeMillis() && !verifying) {
                error = "验证码已过期，正在刷新"
                challengeId = ""
                load()
            }
        }
    }
    AlertDialog(
        onDismissRequest = { if (!verifying) onDismiss() },
        title = { Text("安全验证") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
                when {
                    loading -> CircularProgressIndicator(Modifier.testTag("captcha_loading"))
                    captchaImage != null -> Image(
                        bitmap = captchaImage,
                        contentDescription = "安全验证码图片",
                        modifier = Modifier.size(StkTokens.CaptchaImageWidth, StkTokens.CaptchaImageHeight).testTag("captcha_image"),
                        contentScale = ContentScale.FillBounds,
                    )
                    else -> Text("验证码图片暂不可用")
                }
                OutlinedTextField(
                    answer,
                    { answer = it.uppercase(Locale.ROOT).take(6) },
                    Modifier.fillMaxWidth().testTag("captcha_input"),
                    enabled = !loading && !verifying && challengeId.isNotBlank(),
                    label = { Text("安全验证码") },
                )
                if (error.isNotBlank()) Text(error, Modifier.testTag("captcha_error"), color = StkTokens.BrandAccent)
                TextButton(onClick = ::load, Modifier.testTag("captcha_refresh"), enabled = !loading && !verifying) { Text("刷新") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    verifying = true
                    error = ""
                    api.post("/v1/auth/captcha/verify", JSONObject().put("challenge_id", challengeId).put("answer", answer).put("action", action).put("device_id", deviceId).toString()) { result ->
                        verifying = false
                        result.onSuccess { raw ->
                            runCatching { JSONObject(raw).getJSONObject("data").getString("captcha_ticket") }
                                .onSuccess(onVerified)
                                .onFailure { error = "验证码验证响应无效" }
                        }.onFailure { failure ->
                            answer = ""
                            if (failure.message.orEmpty().contains("CAPTCHA_EXPIRED")) {
                                error = "验证码已过期，正在刷新"
                                challengeId = ""
                                load()
                            } else {
                                error = "验证码错误，请重新输入"
                            }
                        }
                    }
                },
                Modifier.testTag("captcha_confirm"),
                enabled = !loading && !verifying && challengeId.isNotBlank() && answer.isNotBlank(),
            ) { if (verifying) CircularProgressIndicator() else Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss, Modifier.testTag("captcha_cancel"), enabled = !verifying) { Text("取消") } },
    )
}

internal fun decodeCaptchaDataUri(value: String): ImageBitmap? = runCatching {
    require(value.startsWith("data:image/png;base64,"))
    val bytes = Base64.decode(value.substringAfter("base64,"), Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() ?: error("Invalid captcha PNG")
}.getOrNull()

@Composable
private fun RegisterScreen(onBack: () -> Unit, onAgreement: () -> Unit, onPrivacy: () -> Unit, onSuccess: () -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }; var agreed by rememberSaveable { mutableStateOf(false) }; var error by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
        Text("注册商推客", style = MaterialTheme.typography.headlineSmall); Text("注册不会发送短信", color = StkTokens.TextSecondary)
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth().testTag("register_phone"), label = { Text("手机号") }); OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("register_password"), label = { Text("密码") }); OutlinedTextField(confirm, { confirm = it }, Modifier.fillMaxWidth().testTag("register_password_confirm"), label = { Text("确认密码") })
        TextButton(onClick = { agreed = !agreed }, Modifier.testTag("register_agreement_check")) { Text(if (agreed) "已同意用户协议和隐私政策" else "请同意用户协议和隐私政策") }
        if (error.isNotBlank()) Text(error, color = StkTokens.BrandAccent)
        Button(onClick = { if (phone.length != 11 || password.length < 8 || password != confirm || !agreed) error = "请检查手机号、密码和协议同意状态" else onSuccess() }, Modifier.fillMaxWidth().testTag("register_submit")) { Text("注册") }
        Row { TextButton(onClick = onAgreement, Modifier.testTag("register_open_agreement")) { Text("用户协议") }; TextButton(onClick = onPrivacy, Modifier.testTag("register_open_privacy")) { Text("隐私政策") }; TextButton(onClick = onBack, Modifier.testTag("register_back_login")) { Text("返回登录") } }
    }
}

@Composable
private fun ResetScreen(onBack: () -> Unit) { var phone by rememberSaveable { mutableStateOf("") }; var sms by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }; var error by rememberSaveable { mutableStateOf("") }; Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { Text("找回密码", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth().testTag("reset_phone"), label = { Text("手机号") }); Button(onClick = { if (phone.length != 11) error = "请输入正确手机号" }, Modifier.fillMaxWidth().testTag("reset_send_sms")) { Text("发送短信验证码") }; OutlinedTextField(sms, { sms = it }, Modifier.fillMaxWidth().testTag("reset_sms_code"), label = { Text("短信验证码") }); OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("reset_new_password"), label = { Text("新密码") }); OutlinedTextField(confirm, { confirm = it }, Modifier.fillMaxWidth().testTag("reset_new_password_confirm"), label = { Text("确认新密码") }); if (error.isNotBlank()) Text(error, color = StkTokens.BrandAccent); Button(onClick = { if (sms.isBlank() || password != confirm || password.length < 8) error = "请检查验证码和新密码" else onBack() }, Modifier.fillMaxWidth().testTag("reset_submit")) { Text("重置密码") }; TextButton(onClick = onBack, Modifier.testTag("reset_back_login")) { Text("返回登录") } } }

@Composable private fun LegalScreen(title: String, onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text(title, style = MaterialTheme.typography.headlineSmall); Text("本协议内容由商推客后台发布并以版本号管理。当前页面用于展示正式协议内容，提交敏感操作前必须确认最新版本。", color = StkTokens.TextSecondary); Button(onClick = onBack, Modifier.testTag(if (title == "用户协议") "agreement_back" else "privacy_back")) { Text("返回") } } }
@Composable private fun HomeScreen(modifier: Modifier, onDetail: () -> Unit) { Column(modifier.fillMaxSize().padding(StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("商推客", style = MaterialTheme.typography.headlineSmall); Text("项目推荐", style = MaterialTheme.typography.titleMedium); LazyColumn(Modifier.testTag("home_project_list"), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { items(listOf("新媒体推广项目", "本地生活合作项目")) { title -> Card(Modifier.fillMaxWidth().testTag("project_card_1")) { Column(Modifier.padding(StkTokens.Space16)) { Text(title, style = MaterialTheme.typography.titleMedium); Text("查看项目详情与联系方式", color = StkTokens.TextSecondary); TextButton(onClick = onDetail) { Text("查看详情") } } } } } } }
@Composable private fun DetailScreen(modifier: Modifier, onBack: () -> Unit) { Column(modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("项目详情", style = MaterialTheme.typography.headlineSmall); Text("项目详情将由服务端返回真实内容。", color = StkTokens.TextSecondary); Button(onClick = onBack, Modifier.testTag("project_detail_back")) { Text("返回首页") } } }
@Composable internal fun StageScreen(modifier: Modifier, onClose: () -> Unit) { Column(modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("项目发布", style = MaterialTheme.typography.headlineSmall); Text("项目发布功能将在 V1.2 开放", color = StkTokens.TextSecondary); Button(onClose, Modifier.fillMaxWidth().testTag("stage_scope_close")) { Text("知道了") } } }
@Composable private fun MeScreen(modifier: Modifier, onLogout: () -> Unit) { Column(modifier.fillMaxSize().padding(StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("我的", style = MaterialTheme.typography.headlineSmall); Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(StkTokens.Space16)) { Text("商推客用户"); Text("UID：待加载", color = StkTokens.TextSecondary); Text("手机号：脱敏显示", color = StkTokens.TextSecondary) } }; OutlinedButton(onClick = onLogout, Modifier.fillMaxWidth().testTag("logout_confirm")) { Text("退出登录") } } }
