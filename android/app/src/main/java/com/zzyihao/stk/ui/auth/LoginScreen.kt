package com.zzyihao.stk.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.zzyihao.stk.data.auth.AuthValidators
import com.zzyihao.stk.data.auth.AuthBootstrapConfig
import com.zzyihao.stk.ui.components.StkFormLabel
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkSegmentedControl
import com.zzyihao.stk.ui.components.StkTextField
import com.zzyihao.stk.ui.components.StkLockGlyph
import com.zzyihao.stk.ui.components.StkPhoneGlyph
import com.zzyihao.stk.ui.components.StkVisibilityGlyph
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

enum class LoginMode { Password, Sms }

@Composable
fun LoginScreen(
    operation: AuthOperationState,
    bootstrap: AuthBootstrapConfig,
    onOpenCaptcha: (String) -> Unit,
    onPasswordLogin: (String, String, String) -> Unit,
    onSendSms: (String, String) -> Unit,
    onSmsLogin: (String, String, String) -> Unit,
    onCloseCaptcha: () -> Unit,
    onRegister: () -> Unit,
    onOpenLegal: (LegalDocument) -> Unit,
    onClearMessage: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(if (bootstrap.defaultLoginMode == "sms") LoginMode.Sms else LoginMode.Password) }
    var mobile by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var smsCode by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var pendingAction by rememberSaveable { mutableStateOf("password_login") }
    var showPasswordRecoveryNotice by rememberSaveable { mutableStateOf(false) }
    var unavailableModeMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val loginModes = listOf(LoginMode.Password, LoginMode.Sms)
    val enabledModes = listOf(bootstrap.passwordEnabled, bootstrap.smsEnabled)
    val loginSubmitting = operation.operation in setOf(AuthOperation.PasswordLogin, AuthOperation.SmsLogin)
    val smsSending = operation.operation == AuthOperation.SmsSending
    val formEnabled = !loginSubmitting && operation.operation != AuthOperation.LoginSuccess
    LaunchedEffect(bootstrap.passwordEnabled, bootstrap.smsEnabled, bootstrap.defaultLoginMode) {
        if (!enabledModes.getOrElse(loginModes.indexOf(mode)) { false }) {
            mode = loginModes.firstOrNull { enabledModes.getOrElse(loginModes.indexOf(it)) { false } } ?: LoginMode.Password
        }
    }

    val mobileError = when {
        submitted -> AuthValidators.mobileError(mobile)
        operation.isError && operation.errorCode == 4001 -> operation.message
        else -> null
    }
    val passwordError = when {
        submitted && mode == LoginMode.Password && AuthValidators.passwordError(password) != null -> AuthValidators.passwordError(password)
        mode == LoginMode.Password && operation.isError && operation.errorCode == 4003 -> operation.message
        else -> null
    }
    val smsError = when {
        submitted && mode == LoginMode.Sms && AuthValidators.smsCodeError(smsCode) != null -> AuthValidators.smsCodeError(smsCode)
        mode == LoginMode.Sms && operation.isError && operation.errorCode in setOf(4003, 4004) -> operation.message
        else -> null
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceBase),
        verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
    ) {
        if (loginModes.size > 1) StkSegmentedControl(
            items = loginModes.map { if (it == LoginMode.Password) "密码登录" else "短信登录" },
            selectedIndex = loginModes.indexOf(mode).coerceAtLeast(0),
            enabledItems = if (formEnabled) enabledModes else List(loginModes.size) { false },
            onSelected = {
                if (enabledModes.getOrElse(it) { false }) {
                    mode = loginModes[it]
                    submitted = false
                    onClearMessage()
                } else {
                    unavailableModeMessage = "短信登录暂未配置，请使用密码登录。"
                }
            },
        )
        if (enabledModes.none { it }) {
            Text("登录方式暂不可用", color = StkColors.Error, style = MaterialTheme.typography.titleMedium)
        }

        StkFormLabel("手机号")
        StkTextField(
            value = mobile,
            onValueChange = { mobile = it.filter(Char::isDigit).take(11); onClearMessage() },
            label = "请输入手机号",
            error = mobileError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingContent = { StkPhoneGlyph(modifier = Modifier.size(StkDimens.IconSmall)) },
            enabled = formEnabled,
        )

        if (mode == LoginMode.Password) {
            StkFormLabel("登录密码")
            StkTextField(
                value = password,
                onValueChange = { password = it.take(32); onClearMessage() },
                label = "请输入登录密码",
                error = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                leadingContent = { StkLockGlyph(modifier = Modifier.size(StkDimens.IconSmall)) },
                enabled = formEnabled,
                trailingContent = {
                    IconButton(onClick = { showPassword = !showPassword }, enabled = formEnabled) {
                        StkVisibilityGlyph(showPassword, modifier = Modifier.size(StkDimens.IconSmall))
                    }
                },
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(StkDimens.SecondaryButtonHeight).clickable { showPasswordRecoveryNotice = true },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text("忘记密码", color = StkColors.BrandPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            StkFormLabel("短信验证码")
            StkTextField(
                value = smsCode,
                onValueChange = { smsCode = it.filter(Char::isDigit).take(6); onClearMessage() },
                label = "请输入短信验证码",
                error = smsError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingContent = { Icon(Icons.Outlined.Shield, null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.IconSmall)) },
                enabled = formEnabled,
                trailingContent = {
                    TextButton(
                        enabled = operation.smsCountdown == 0 && formEnabled && !smsSending,
                        onClick = {
                            submitted = true
                            if (AuthValidators.mobileError(mobile) == null) {
                                pendingAction = "sms_send"
                                onOpenCaptcha("sms_send")
                            }
                        },
                    ) {
                        if (smsSending) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(StkDimens.InlineProgress),
                                    strokeWidth = StkDimens.SplashProgressStroke,
                                    color = StkColors.BrandPrimary,
                                )
                                Text("发送中…", style = MaterialTheme.typography.bodyMedium, color = StkColors.BrandPrimary)
                            }
                        } else {
                            Text(
                                if (operation.smsCountdown > 0) "${operation.smsCountdown}s 后重发" else "发送验证码",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StkColors.BrandPrimary,
                            )
                        }
                    }
                },
            )
            Box(Modifier.height(StkDimens.SpaceXl))
        }

        StkPrimaryButton(
            text = "登录",
            loading = loginSubmitting,
            enabled = enabledModes.getOrElse(loginModes.indexOf(mode)) { false } && formEnabled,
            onClick = {
                submitted = true
                val valid = AuthValidators.mobileError(mobile) == null && when (mode) {
                    LoginMode.Password -> AuthValidators.passwordError(password) == null
                    LoginMode.Sms -> AuthValidators.smsCodeError(smsCode) == null
                }
                if (valid) {
                    pendingAction = if (mode == LoginMode.Password) "password_login" else "sms_login"
                    onOpenCaptcha(pendingAction)
                }
            },
        )

        if (bootstrap.registerEnabled) TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceBase)) { Text("还没有账号？立即注册", style = MaterialTheme.typography.titleMedium, color = StkColors.TextSecondary) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("登录即表示同意 ", style = MaterialTheme.typography.bodySmall, color = StkColors.TextTertiary)
            Text(
                "《用户协议》",
                style = MaterialTheme.typography.bodySmall,
                color = StkColors.TextTertiary,
                modifier = Modifier.clickable { onOpenLegal(LegalDocument.UserAgreement) },
            )
            Text(" 和 ", style = MaterialTheme.typography.bodySmall, color = StkColors.TextTertiary)
            Text(
                "《隐私政策》",
                style = MaterialTheme.typography.bodySmall,
                color = StkColors.TextTertiary,
                modifier = Modifier.clickable { onOpenLegal(LegalDocument.PrivacyPolicy) },
            )
        }

        val feedback = loginFeedback(operation)
        if (feedback != null) {
            StkFeedbackBanner(
                message = feedback.message,
                tone = feedback.tone,
                emphasized = feedback.emphasized,
                actionLabel = feedback.actionLabel,
                onAction = feedback.actionLabel?.let {
                    {
                        onClearMessage()
                        onOpenCaptcha(pendingAction)
                    }
                },
                modifier = Modifier.padding(top = StkDimens.Space2Xl),
            )
        }
    }

    if (operation.captchaVisible) {
        CaptchaDialog(
            challenge = operation.captcha,
            operation = operation,
            onDismiss = onCloseCaptcha,
            onRefresh = { onOpenCaptcha(pendingAction) },
            onConfirm = { captchaCode ->
                when (pendingAction) {
                    "password_login" -> onPasswordLogin(mobile, password, captchaCode)
                    "sms_send" -> onSendSms(mobile, captchaCode)
                    "sms_login" -> onSmsLogin(mobile, smsCode, captchaCode)
                }
            },
        )
    }
    if (showPasswordRecoveryNotice) {
        AlertDialog(
            onDismissRequest = { showPasswordRecoveryNotice = false },
            title = { Text("找回密码暂未开放") },
            text = {
                Text(
                    if (bootstrap.smsEnabled) {
                        "当前版本尚未提供自助找回密码。请使用已绑定的短信登录，或联系管理员协助处理。"
                    } else {
                        "当前版本尚未提供自助找回密码，短信登录也尚未配置。请联系管理员协助处理。"
                    },
                )
            },
            confirmButton = { TextButton(onClick = { showPasswordRecoveryNotice = false }) { Text("知道了") } },
            containerColor = StkColors.Surface,
        )
    }
    unavailableModeMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { unavailableModeMessage = null },
            title = { Text("短信登录不可用") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { unavailableModeMessage = null }) { Text("知道了") } },
            containerColor = StkColors.Surface,
        )
    }
}

private data class LoginFeedback(
    val message: String,
    val tone: StkFeedbackTone,
    val emphasized: Boolean,
    val actionLabel: String? = null,
)

private fun loginFeedback(operation: AuthOperationState): LoginFeedback? {
    if (operation.risk != AuthRisk.None) return null
    val message = operation.message ?: return null
    return when {
        operation.operation == AuthOperation.LoginSuccess -> LoginFeedback(
            message = "登录成功，正在进入首页",
            tone = StkFeedbackTone.Success,
            emphasized = true,
        )
        !operation.isError && operation.activeScene == "sms_send" -> LoginFeedback(
            message = message,
            tone = StkFeedbackTone.Success,
            emphasized = false,
        )
        operation.issue == AuthIssue.RateLimited -> LoginFeedback(
            message = message,
            tone = StkFeedbackTone.Warning,
            emphasized = false,
        )
        operation.issue == AuthIssue.Network -> LoginFeedback(
            message = message,
            tone = StkFeedbackTone.Error,
            emphasized = true,
            actionLabel = "重试",
        )
        operation.issue == AuthIssue.Timeout -> LoginFeedback(
            message = message,
            tone = StkFeedbackTone.Warning,
            emphasized = true,
            actionLabel = "重试",
        )
        else -> null
    }
}
