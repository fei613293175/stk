package com.zzyihao.stk.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzyihao.stk.data.auth.AuthRepository
import com.zzyihao.stk.data.auth.AuthBootstrapConfig
import com.zzyihao.stk.data.legal.LegalRepository
import com.zzyihao.stk.ui.components.StkBrandMark
import com.zzyihao.stk.ui.components.StkBackGlyph
import com.zzyihao.stk.ui.components.StkStatusDialog
import com.zzyihao.stk.ui.components.StkStatusKind
import com.zzyihao.stk.ui.theme.StkDimens
import com.zzyihao.stk.ui.theme.StkColors
import kotlinx.coroutines.launch

enum class AuthPage { Login, Register }

@Composable
fun AuthEntryScreen(
    repository: AuthRepository,
    legalRepository: LegalRepository,
    bootstrap: AuthBootstrapConfig,
    initialPage: AuthPage,
    onPageChanged: (AuthPage) -> Unit,
    onAuthenticationRouteSelected: (String) -> Unit,
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
    val operation by authViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var page by rememberSaveable(initialPage) { mutableStateOf(initialPage) }
    var legalDocument by rememberSaveable { mutableStateOf<LegalDocument?>(null) }
    val clearFeedback: () -> Unit = {
        authViewModel.clearMessage()
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    LaunchedEffect(bootstrap.registerEnabled) {
        if (!bootstrap.registerEnabled && page == AuthPage.Register) {
            page = AuthPage.Login
            onPageChanged(page)
        }
    }

    LaunchedEffect(operation.message) {
        val message = operation.message ?: return@LaunchedEffect
        if (operation.errorCode == 4030) return@LaunchedEffect
        if (operation.errorCode in setOf(4001, 4003, 4004, 4005)) return@LaunchedEffect
        if (operation.issue in setOf(AuthIssue.RateLimited, AuthIssue.Network, AuthIssue.Timeout, AuthIssue.Server)) {
            return@LaunchedEffect
        }
        if (page == AuthPage.Login && (
                    operation.operation == AuthOperation.LoginSuccess ||
                    (!operation.isError && operation.activeScene == "sms_send")
                )
        ) return@LaunchedEffect
        // Keep a failed captcha validation visible inside the dialog until the
        // user changes challenge, retries, or dismisses it.
        if (operation.captcha != null && operation.isError) return@LaunchedEffect
        scope.launch { snackbarHostState.showSnackbar(message) }
        authViewModel.clearMessage()
    }

    if (legalDocument != null) {
        LegalScreen(document = legalDocument!!, repository = legalRepository, onBack = { legalDocument = null })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StkColors.Surface)
            .systemBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = StkDimens.ContentMaxWidth)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = StkDimens.AuthHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (page == AuthPage.Login) StkDimens.AuthHeroTop else StkDimens.RegisterHeroTop),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StkBrandMark()
                Text(
                    text = if (page == AuthPage.Login) "欢迎使用商推客" else "创建商推客账号",
                    style = MaterialTheme.typography.headlineLarge,
                    color = StkColors.TextPrimary,
                    modifier = Modifier.padding(top = StkDimens.SpaceXl),
                )
                Text(
                    text = if (page == AuthPage.Login) "登录后发现和发布推广项目" else "注册不发送短信验证码",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StkColors.TextSecondary,
                    modifier = Modifier.padding(top = StkDimens.SpaceXs),
                )
            }

            when (page) {
                AuthPage.Login -> LoginScreen(
                    operation = operation,
                    bootstrap = bootstrap,
                    onOpenCaptcha = authViewModel::openCaptcha,
                    onPasswordLogin = { mobile, password, code ->
                        onAuthenticationRouteSelected(bootstrap.loginSuccessRoute)
                        authViewModel.passwordLogin(mobile, password, code)
                    },
                    onSendSms = authViewModel::sendSms,
                    onSmsLogin = { mobile, smsCode, code ->
                        onAuthenticationRouteSelected(bootstrap.loginSuccessRoute)
                        authViewModel.smsLogin(mobile, smsCode, code)
                    },
                    onCloseCaptcha = authViewModel::closeCaptcha,
                    onRegister = {
                        authViewModel.closeCaptcha()
                        clearFeedback()
                        page = AuthPage.Register
                        onPageChanged(page)
                    },
                    onOpenLegal = { legalDocument = it },
                    onClearMessage = clearFeedback,
                )
                AuthPage.Register -> if (bootstrap.registerEnabled) RegisterScreen(
                    operation = operation,
                    onOpenCaptcha = authViewModel::openCaptcha,
                    onRegister = { mobile, password, confirmation, code ->
                        onAuthenticationRouteSelected(bootstrap.registerSuccessRoute)
                        authViewModel.register(mobile, password, confirmation, code)
                    },
                    onCloseCaptcha = authViewModel::closeCaptcha,
                    onBackToLogin = {
                        authViewModel.closeCaptcha()
                        clearFeedback()
                        page = AuthPage.Login
                        onPageChanged(page)
                    },
                    onOpenLegal = { legalDocument = it },
                    onClearMessage = clearFeedback,
                    onCompleteRegistration = authViewModel::completeRegistration,
                ) else LoginScreen(
                    operation = operation,
                    bootstrap = bootstrap,
                    onOpenCaptcha = authViewModel::openCaptcha,
                    onPasswordLogin = { mobile, password, code ->
                        onAuthenticationRouteSelected(bootstrap.loginSuccessRoute)
                        authViewModel.passwordLogin(mobile, password, code)
                    },
                    onSendSms = authViewModel::sendSms,
                    onSmsLogin = { mobile, smsCode, code ->
                        onAuthenticationRouteSelected(bootstrap.loginSuccessRoute)
                        authViewModel.smsLogin(mobile, smsCode, code)
                    },
                    onCloseCaptcha = authViewModel::closeCaptcha,
                    onRegister = {},
                    onOpenLegal = { legalDocument = it },
                    onClearMessage = clearFeedback,
                )
            }
        }
        if (page == AuthPage.Register) {
            IconButton(
                onClick = {
                    authViewModel.closeCaptcha()
                    clearFeedback()
                    page = AuthPage.Login
                    onPageChanged(page)
                },
                modifier = Modifier.align(Alignment.TopStart).padding(top = StkDimens.SpaceSm),
                enabled = !operation.loading,
            ) {
                StkBackGlyph(modifier = Modifier.size(StkDimens.Icon), tint = StkColors.TextPrimary)
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = StkDimens.SpaceBase),
        )
    }
    riskDialogContent(operation)?.let { content ->
        StkStatusDialog(
            title = content.title,
            message = content.message,
            kind = content.kind,
            buttonLabel = "我知道了",
            onDismiss = { authViewModel.closeCaptcha(); authViewModel.clearMessage() },
        )
    }
}

private data class RiskDialogContent(
    val title: String,
    val message: String,
    val kind: StkStatusKind,
)

private fun riskDialogContent(operation: AuthOperationState): RiskDialogContent? = when (operation.risk) {
    AuthRisk.None -> null
    AuthRisk.LoginLocked -> RiskDialogContent(
        title = "登录暂时锁定",
        message = operation.retryCountdown.takeIf { it > 0 }
            ?.let { "多次尝试失败，请在 ${formatRetryDuration(it)}后重试。" }
            ?: "多次尝试失败，请稍后重试。",
        kind = StkStatusKind.Time,
    )
    AuthRisk.AccountDisabled -> RiskDialogContent(
        title = "账号当前不可用",
        message = "请通过客服页面了解账号处理方式。",
        kind = StkStatusKind.Error,
    )
    AuthRisk.ManualReview -> RiskDialogContent(
        title = "需要进一步核验",
        message = "为保护账号安全，本次登录需要人工核验。",
        kind = StkStatusKind.Warning,
    )
    AuthRisk.MethodUnavailable -> RiskDialogContent(
        title = "当前登录方式不可用",
        message = "请切换到另一种登录方式后重试。",
        kind = StkStatusKind.Info,
    )
}

internal fun formatRetryDuration(totalSeconds: Int): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val minutes = seconds / 60
    val remainder = seconds % 60
    return if (minutes > 0) "$minutes 分 $remainder 秒" else "$remainder 秒"
}
