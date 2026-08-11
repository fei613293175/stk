package com.zzyihao.stk.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.zzyihao.stk.BuildConfig
import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.refreshDelayMillis
import com.zzyihao.stk.data.auth.AuthException
import com.zzyihao.stk.data.auth.AuthBootstrapConfig
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.auth.UserSummary
import com.zzyihao.stk.di.AppContainer
import com.zzyihao.stk.ui.auth.AuthEntryScreen
import com.zzyihao.stk.ui.auth.AuthPage
import com.zzyihao.stk.ui.main.MainShell
import com.zzyihao.stk.ui.components.LogoutDialogState
import com.zzyihao.stk.ui.components.StkLogoutDialog
import com.zzyihao.stk.ui.components.StkStatusDialog
import com.zzyihao.stk.ui.components.StkStatusKind
import com.zzyihao.stk.ui.system.SplashScreen
import com.zzyihao.stk.ui.system.SystemFeedbackScreen
import com.zzyihao.stk.ui.system.SystemFeedbackState
import com.zzyihao.stk.ui.system.ReleaseUpdateOverlay
import com.zzyihao.stk.ui.system.ReleaseUpdateScreen
import com.zzyihao.stk.ui.system.StartupGate
import com.zzyihao.stk.ui.system.StartupVisualState
import com.zzyihao.stk.ui.system.StartupViewModel
import kotlinx.coroutines.delay

internal const val MINIMUM_SPLASH_MILLIS = 800L
private const val STARTUP_TRANSITION_MILLIS = 400L

private sealed interface AuthBootstrapLoadState {
    data object Loading : AuthBootstrapLoadState
    data class Ready(val config: AuthBootstrapConfig) : AuthBootstrapLoadState
    data class Error(val message: String) : AuthBootstrapLoadState
}

@Composable
fun StkApp(
    container: AppContainer,
    startupFeedbackState: SystemFeedbackState? = null,
    startupRequestId: String? = null,
    onStartupFeedbackDismiss: () -> Unit = {},
    deepLinkProjectId: String? = null,
    deepLinkUpdateRequested: Boolean = false,
) {
    val sessionState by container.tokenStore.session.collectAsStateWithLifecycle(
        initialValue = SessionState.Loading,
    )
    var authPage by rememberSaveable { mutableStateOf(AuthPage.Login) }
    var authBootstrapState by remember { mutableStateOf<AuthBootstrapLoadState>(AuthBootstrapLoadState.Loading) }
    var authBootstrapRetry by rememberSaveable { mutableIntStateOf(0) }
    var authenticatedRoute by rememberSaveable { mutableStateOf("stk://home") }
    var failedRefreshToken by remember { mutableStateOf<String?>(null) }
    var minimumSplashElapsed by rememberSaveable { mutableStateOf(false) }
    var enteredAuthenticatedApp by remember { mutableStateOf(false) }
    var hadLoggedInSession by remember { mutableStateOf(false) }
    var manualLogout by remember { mutableStateOf(false) }
    var sessionExpired by remember { mutableStateOf(false) }
    var logoutFailureMessage by remember { mutableStateOf<String?>(null) }
    var activeMainSectionIsMe by remember { mutableStateOf(false) }
    var startupUpdatePageVisible by rememberSaveable { mutableStateOf(false) }
    val startupViewModel: StartupViewModel = viewModel(
        key = "startup-${if (deepLinkUpdateRequested) "update" else "normal"}",
        factory = StartupViewModel.Factory(
            releaseRepository = container.releaseRepository,
            installedVersionCode = BuildConfig.VERSION_CODE,
            showUpToDate = deepLinkUpdateRequested,
        ),
    )
    val startupGate by startupViewModel.gate.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(MINIMUM_SPLASH_MILLIS)
        minimumSplashElapsed = true
    }

    LaunchedEffect(sessionState, minimumSplashElapsed, startupGate, authBootstrapRetry) {
        if (sessionState == SessionState.LoggedOut && minimumSplashElapsed && startupGate == StartupGate.Continue) {
            authPage = AuthPage.Login
            authenticatedRoute = "stk://home"
            authBootstrapState = AuthBootstrapLoadState.Loading
            val startedAt = System.currentTimeMillis()
            val result = runCatching { container.authRepository.getBootstrap() }
            delay((STARTUP_TRANSITION_MILLIS - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0L))
            authBootstrapState = result.fold(
                onSuccess = { config ->
                    if (!config.registerEnabled && authPage == AuthPage.Register) authPage = AuthPage.Login
                    AuthBootstrapLoadState.Ready(config)
                },
                onFailure = { AuthBootstrapLoadState.Error(it.message ?: "启动配置加载失败") },
            )
        }
    }

    LaunchedEffect(sessionState, minimumSplashElapsed, startupGate) {
        when {
            sessionState == SessionState.LoggedOut -> {
                enteredAuthenticatedApp = false
                if (hadLoggedInSession && !manualLogout) sessionExpired = true
                if (manualLogout) hadLoggedInSession = false
                manualLogout = false
            }
            sessionState is SessionState.LoggedIn &&
                minimumSplashElapsed &&
                startupGate == StartupGate.Continue &&
                !enteredAuthenticatedApp -> {
                hadLoggedInSession = true
                sessionExpired = false
                delay(STARTUP_TRANSITION_MILLIS)
                enteredAuthenticatedApp = true
            }
        }
    }

    val performLogout: suspend () -> Unit = {
        manualLogout = true
        logoutFailureMessage = null
        runCatching { container.authRepository.logout() }
            .onFailure { logoutFailureMessage = it.message ?: "服务器撤销失败" }
    }

    if (startupFeedbackState != null) {
        SystemFeedbackScreen(
            initialState = startupFeedbackState,
            requestId = startupRequestId,
            onBack = onStartupFeedbackDismiss,
            onRetry = onStartupFeedbackDismiss,
        )
        return
    }
    if (!minimumSplashElapsed) {
        SplashScreen()
        return
    }
    if ((sessionExpired || logoutFailureMessage != null) && startupGate == StartupGate.Continue) {
        MainShell(
            session = redactedSession(),
            onLogout = {},
            projectRepository = container.projectRepository,
            projectSubmissionRepository = container.projectSubmissionRepository,
            accountRepository = container.accountRepository,
            legalRepository = container.legalRepository,
            releaseRepository = container.releaseRepository,
            onClearCache = container::clearCachedData,
            onMeSectionChanged = { activeMainSectionIsMe = it },
            initialRoute = if (activeMainSectionIsMe) "stk://me" else "stk://home",
        )
        if (sessionExpired) {
            StkStatusDialog(
                title = "登录状态已过期",
                message = if (activeMainSectionIsMe) {
                    "敏感资料已从页面清理，请重新登录。"
                } else {
                    "为保护账号安全，请重新登录后继续使用。"
                },
                kind = StkStatusKind.Session,
                buttonLabel = "重新登录",
                onDismiss = {
                    sessionExpired = false
                    hadLoggedInSession = false
                    authPage = AuthPage.Login
                },
            )
        } else {
            StkLogoutDialog(
                state = LogoutDialogState.Failed,
                onDismiss = {},
                onConfirm = {
                    logoutFailureMessage = null
                    hadLoggedInSession = false
                    authPage = AuthPage.Login
                },
            )
        }
        return
    }
    when (val gate = startupGate) {
        StartupGate.Checking -> SplashScreen()
        is StartupGate.Failure -> if (sessionState is SessionState.LoggedIn || sessionState is SessionState.NeedsRefresh) {
            SplashScreen(
                state = StartupVisualState.Offline,
                message = "当前无法连接服务，可继续查看最近缓存内容。",
                onRetry = startupViewModel::continueToApp,
                actionLabel = "离线进入",
            )
        } else {
            SystemFeedbackScreen(
                initialState = SystemFeedbackState.Offline,
                message = "首次使用且没有可用缓存，请连接网络后继续。",
                onRetry = startupViewModel::check,
            )
        }
        is StartupGate.Feedback -> when (gate.state) {
            SystemFeedbackState.OptionalUpdate, SystemFeedbackState.ForcedUpdate -> {
                val forced = gate.state == SystemFeedbackState.ForcedUpdate
                if (startupUpdatePageVisible) {
                    ReleaseUpdateScreen(
                        releaseRepository = container.releaseRepository,
                        initialRelease = gate.release,
                        initialForced = forced,
                        onBack = if (forced) null else {
                            {
                                startupUpdatePageVisible = false
                                startupViewModel.continueToApp()
                            }
                        },
                    )
                } else {
                    Box(Modifier.fillMaxSize()) {
                        SplashScreen(state = StartupVisualState.Ready)
                        ReleaseUpdateOverlay(
                            release = gate.release,
                            forced = forced,
                            onLater = if (forced) null else startupViewModel::continueToApp,
                            onOpenUpdate = { startupUpdatePageVisible = true },
                        )
                    }
                }
            }
            SystemFeedbackState.UpToDate -> ReleaseUpdateScreen(
                releaseRepository = container.releaseRepository,
                initialRelease = null,
                initialForced = false,
                onBack = startupViewModel::continueToApp,
            )
            SystemFeedbackState.ServiceRecovered -> SystemFeedbackScreen(
                initialState = gate.state,
                release = gate.release,
                message = gate.message,
                onRetry = startupViewModel::continueToApp,
            )
            else -> SystemFeedbackScreen(
                initialState = gate.state,
                release = gate.release,
                message = gate.message,
                onRetry = { startupViewModel.check(showRecoveredOnSuccess = true) },
            )
        }
        StartupGate.Continue -> when (val state = sessionState) {
            SessionState.Loading -> SplashScreen()
            is SessionState.NeedsRefresh -> {
                if (failedRefreshToken == state.refreshToken) {
                    SystemFeedbackScreen(
                        initialState = SystemFeedbackState.Offline,
                        onRetry = { failedRefreshToken = null },
                    )
                } else {
                    LaunchedEffect(state.refreshToken) {
                        if (!rotateSession(container, state.refreshToken, maxAttempts = 4)) {
                            failedRefreshToken = state.refreshToken
                        }
                    }
                    SplashScreen()
                }
            }
            is SessionState.LoggedIn -> {
                LaunchedEffect(state.session.accessToken, state.session.expiresAt) {
                    delay(refreshDelayMillis(state.session.expiresAt, System.currentTimeMillis()))
                    rotateSession(container, state.session.refreshToken)
                }
                if (!enteredAuthenticatedApp) {
                    SplashScreen(state = StartupVisualState.Ready)
                } else {
                    MainShell(
                        session = state.session,
                        onLogout = performLogout,
                        projectRepository = container.projectRepository,
                        projectSubmissionRepository = container.projectSubmissionRepository,
                        accountRepository = container.accountRepository,
                        legalRepository = container.legalRepository,
                        releaseRepository = container.releaseRepository,
                        onClearCache = container::clearCachedData,
                        onMeSectionChanged = { activeMainSectionIsMe = it },
                        initialDetailId = deepLinkProjectId,
                        initialRoute = authenticatedRoute,
                    )
                }
            }
            SessionState.LoggedOut -> when (val bootstrap = authBootstrapState) {
                AuthBootstrapLoadState.Loading -> SplashScreen(state = StartupVisualState.NeedsLogin)
                is AuthBootstrapLoadState.Error -> SplashScreen(
                    state = StartupVisualState.Error,
                    message = bootstrap.message,
                    onRetry = { authBootstrapRetry++ },
                )
                is AuthBootstrapLoadState.Ready -> AuthEntryScreen(
                    repository = container.authRepository,
                    legalRepository = container.legalRepository,
                    bootstrap = bootstrap.config,
                    initialPage = authPage,
                    onPageChanged = { authPage = it },
                    onAuthenticationRouteSelected = { authenticatedRoute = it },
                )
            }
        }
    }
}

private fun redactedSession(): AuthSession = AuthSession(
    accessToken = "",
    refreshToken = "",
    expiresAt = 0L,
    user = UserSummary(
        uid = 0L,
        username = "账号已退出",
        mobileMasked = "",
        memberLabel = "",
    ),
)

private suspend fun rotateSession(container: AppContainer, refreshToken: String, maxAttempts: Int? = null): Boolean =
    rotateSession(
        refreshToken = refreshToken,
        maxAttempts = maxAttempts,
        refresh = { container.authRepository.refreshSession(it) },
        clear = container.tokenStore::clear,
        pause = { delay(it) },
    )

internal suspend fun rotateSession(
    refreshToken: String,
    maxAttempts: Int? = null,
    refresh: suspend (String) -> Unit,
    clear: suspend () -> Unit,
    pause: suspend (Long) -> Unit,
): Boolean {
    var retryDelay = 2_000L
    var attempts = 0
    while (maxAttempts == null || attempts < maxAttempts) {
        try {
            refresh(refreshToken)
            return true
        } catch (error: AuthException) {
            if (error.code in setOf(4010, 4011)) {
                clear()
                return true
            }
        } catch (_: Exception) {
            // Keep the refresh token during temporary network outages.
        }
        attempts += 1
        if (maxAttempts != null && attempts >= maxAttempts) break
        pause(retryDelay)
        retryDelay = (retryDelay * 2).coerceAtMost(30_000L)
    }
    return false
}
