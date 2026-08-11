package com.zzyihao.stk.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.zzyihao.stk.data.auth.AuthValidators
import com.zzyihao.stk.ui.components.StkFormLabel
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTextField
import com.zzyihao.stk.ui.components.StkRegistrationSuccessDialog
import com.zzyihao.stk.ui.components.StkLockGlyph
import com.zzyihao.stk.ui.components.StkPhoneGlyph
import com.zzyihao.stk.ui.components.StkVisibilityGlyph
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

@Composable
fun RegisterScreen(
    operation: AuthOperationState,
    onOpenCaptcha: (String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onCloseCaptcha: () -> Unit,
    onBackToLogin: () -> Unit,
    onOpenLegal: (LegalDocument) -> Unit,
    onClearMessage: () -> Unit,
    onCompleteRegistration: () -> Unit,
) {
    var mobile by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var agreementAccepted by rememberSaveable { mutableStateOf(true) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val registering = operation.operation == AuthOperation.Registering
    val formEnabled = !registering
    val localMobileError = if (submitted) AuthValidators.mobileError(mobile) else null
    val mobileError = localMobileError ?: operation.message.takeIf {
        operation.isError && operation.errorCode == 4005
    }
    val agreementError = submitted && !agreementAccepted

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceBase),
        verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
    ) {
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
        StkFormLabel("登录密码")
        StkTextField(
            value = password,
            onValueChange = { password = it.take(32); onClearMessage() },
            label = "8-20 位字母与数字",
            error = if (submitted) {
                AuthValidators.passwordError(password)?.let {
                    if (password.isBlank()) it else "密码需为 8-20 位字母与数字组合"
                }
            } else {
                null
            },
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
        StkFormLabel("确认登录密码")
        StkTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it.take(32); onClearMessage() },
            label = "请再次输入密码",
            error = if (submitted) AuthValidators.confirmPasswordError(password, confirmPassword) else null,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (agreementError) StkColors.ErrorSoft else StkColors.Surface,
                    RoundedCornerShape(StkDimens.RadiusSmall),
                )
                .then(
                    if (agreementError) {
                        Modifier.border(StkDimens.Divider, StkColors.Error, RoundedCornerShape(StkDimens.RadiusSmall))
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = StkDimens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Checkbox(
                    checked = agreementAccepted,
                    enabled = formEnabled,
                    onCheckedChange = { agreementAccepted = it; onClearMessage() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StkColors.BrandPrimary,
                        checkmarkColor = StkColors.Surface,
                        uncheckedColor = if (agreementError) StkColors.Error else StkColors.Border,
                    ),
                    modifier = Modifier.semantics { contentDescription = "同意协议" },
                )
            }
            Text("我已阅读并同意", style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
            Text(
                "《用户协议》",
                style = MaterialTheme.typography.bodySmall,
                color = StkColors.TextSecondary,
                modifier = Modifier.clickable(enabled = formEnabled) { onOpenLegal(LegalDocument.UserAgreement) },
            )
            Text("和", style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
            Text(
                "《隐私政策》",
                style = MaterialTheme.typography.bodySmall,
                color = StkColors.TextSecondary,
                modifier = Modifier.clickable(enabled = formEnabled) { onOpenLegal(LegalDocument.PrivacyPolicy) },
            )
        }
        if (agreementError) Text("请先阅读并同意协议", color = StkColors.Error, style = MaterialTheme.typography.bodyMedium)
        StkPrimaryButton(
            text = "注册",
            loading = registering,
            loadingText = "注册中",
            enabled = formEnabled,
            onClick = {
                submitted = true
                val valid = AuthValidators.mobileError(mobile) == null &&
                    AuthValidators.passwordError(password) == null &&
                    AuthValidators.confirmPasswordError(password, confirmPassword) == null &&
                    agreementAccepted
                if (valid) onOpenCaptcha("register")
            },
        )
        TextButton(
            onClick = onBackToLogin,
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceBase),
        ) { Text("已有账号？返回登录", style = MaterialTheme.typography.titleMedium, color = StkColors.TextSecondary) }

        val feedback = registrationFeedback(operation)
        if (feedback != null) {
            StkFeedbackBanner(
                message = feedback.message,
                tone = feedback.tone,
                emphasized = feedback.emphasized,
                actionLabel = feedback.actionLabel,
                onAction = feedback.actionLabel?.let {
                    {
                        onClearMessage()
                        onOpenCaptcha("register")
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
            onRefresh = { onOpenCaptcha("register") },
            onConfirm = { code -> onRegister(mobile, password, confirmPassword, code) },
        )
    }
    if (operation.pendingRegistrationSession != null) {
        StkRegistrationSuccessDialog(
            loading = operation.loading,
            onContinue = onCompleteRegistration,
        )
    }
}

private data class RegistrationFeedback(
    val message: String,
    val tone: StkFeedbackTone,
    val emphasized: Boolean,
    val actionLabel: String? = null,
)

private fun registrationFeedback(operation: AuthOperationState): RegistrationFeedback? {
    val message = operation.message ?: return null
    return when (operation.issue) {
        AuthIssue.RateLimited -> RegistrationFeedback(message, StkFeedbackTone.Warning, emphasized = false)
        AuthIssue.Network -> RegistrationFeedback(message, StkFeedbackTone.Error, emphasized = true, actionLabel = "重试")
        AuthIssue.Timeout -> RegistrationFeedback(message, StkFeedbackTone.Warning, emphasized = true, actionLabel = "重试")
        AuthIssue.Server -> RegistrationFeedback(message, StkFeedbackTone.Error, emphasized = false, actionLabel = "重试")
        else -> null
    }
}
