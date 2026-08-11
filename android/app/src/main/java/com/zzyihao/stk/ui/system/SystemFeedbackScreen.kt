package com.zzyihao.stk.ui.system

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzyihao.stk.BuildConfig
import com.zzyihao.stk.data.release.ApkUpdateManager
import com.zzyihao.stk.data.release.DownloadProgress
import com.zzyihao.stk.data.release.ReleaseManifest
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.ui.components.StkBrandMark
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SystemFeedbackState {
    UpToDate,
    OptionalUpdate,
    ForcedUpdate,
    Downloading,
    InstallBlocked,
    Maintenance,
    ServiceRecovered,
    Offline,
    SystemError,
}

sealed interface UpdateOverlayState {
    data object Available : UpdateOverlayState
    data class Downloading(val progress: DownloadProgress) : UpdateOverlayState
    data class Failed(val message: String) : UpdateOverlayState
}

@Composable
fun SystemFeedbackScreen(
    initialState: SystemFeedbackState = SystemFeedbackState.UpToDate,
    release: ReleaseManifest? = null,
    message: String? = null,
    requestId: String? = null,
    onBack: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
) {
    if (release != null && initialState in setOf(SystemFeedbackState.OptionalUpdate, SystemFeedbackState.ForcedUpdate)) {
        Box(Modifier.fillMaxSize()) {
            SplashScreen(state = StartupVisualState.Ready)
            ReleaseUpdateOverlay(
                release = release,
                forced = initialState == SystemFeedbackState.ForcedUpdate,
                onLater = onBack,
                onOpenUpdate = onRetry ?: {},
            )
        }
        return
    }

    val context = LocalContext.current
    val content = feedbackContent(initialState, release, message)
    val exitApp: () -> Unit = {
        (context as? Activity)?.finishAffinity()
        Unit
    }
    Column(Modifier.fillMaxSize().background(StkColors.Background)) {
        StkTopBar(title = "系统状态")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SystemFeedbackBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            StatusIcon(content.icon, content.tint)
            Text(
                content.title,
                style = MaterialTheme.typography.headlineLarge,
                color = StkColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = StkDimens.SpaceXl),
            )
            Text(
                content.message,
                style = MaterialTheme.typography.bodyLarge,
                color = StkColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = StkDimens.SpaceSm),
            )
            if (initialState == SystemFeedbackState.SystemError && !requestId.isNullOrBlank()) {
                Text(
                    "请求编号：$requestId",
                    style = MaterialTheme.typography.bodySmall,
                    color = StkColors.TextTertiary,
                    modifier = Modifier.padding(top = StkDimens.SpaceXl),
                )
            }
            onRetry?.let { callback ->
                StkPrimaryButton(
                    text = when (initialState) {
                        SystemFeedbackState.ServiceRecovered -> "继续使用"
                        SystemFeedbackState.Maintenance -> "稍后重试"
                        else -> "重新连接"
                    },
                    onClick = callback,
                    modifier = Modifier.padding(top = StkDimens.SpaceXl).width(StkDimens.SystemFeedbackButtonWidth),
                )
            }
            if (initialState == SystemFeedbackState.Maintenance) {
                OutlinedButton(
                    onClick = exitApp,
                    modifier = Modifier
                        .padding(top = StkDimens.SpaceSm)
                        .width(StkDimens.SystemFeedbackButtonWidth)
                        .height(StkDimens.PrimaryControlHeight),
                    shape = RoundedCornerShape(StkDimens.RadiusControl),
                ) {
                    Text("退出应用", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
                }
            } else if (onBack != null) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = StkDimens.SpaceSm)
                        .width(StkDimens.SystemFeedbackButtonWidth)
                        .height(StkDimens.PrimaryControlHeight),
                    shape = RoundedCornerShape(StkDimens.RadiusControl),
                ) {
                    Text("返回", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
fun ReleaseUpdateOverlay(
    release: ReleaseManifest,
    forced: Boolean,
    state: UpdateOverlayState = UpdateOverlayState.Available,
    onLater: (() -> Unit)? = null,
    onOpenUpdate: () -> Unit,
    onCancelDownload: (() -> Unit)? = null,
    onContactSupport: (() -> Unit)? = null,
) {
    BackHandler(enabled = forced) {}
    val canDismiss = !forced && state !is UpdateOverlayState.Downloading
    Dialog(
        onDismissRequest = { if (canDismiss) onLater?.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth(StkDimens.DialogWidthFraction),
                shape = RoundedCornerShape(StkDimens.RadiusDialog),
                color = StkColors.Surface,
            ) {
                Column(
                    modifier = Modifier.padding(StkDimens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase),
                ) {
                    val failed = state is UpdateOverlayState.Failed
                    StatusIcon(
                        icon = if (failed) Icons.Default.ErrorOutline else Icons.Default.Download,
                        tint = when {
                            failed -> StkColors.Error
                            forced -> StkColors.Warning
                            else -> StkColors.BrandPrimary
                        },
                        compact = true,
                    )
                    Text(
                        text = when (state) {
                            UpdateOverlayState.Available -> if (forced) "需要更新后继续使用" else "发现新版本 ${release.versionName}"
                            is UpdateOverlayState.Downloading -> "正在下载更新"
                            is UpdateOverlayState.Failed -> "更新下载失败"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = StkColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = when (state) {
                            UpdateOverlayState.Available -> if (forced) {
                                "当前版本低于最低可用版本，必须更新后继续。"
                            } else {
                                release.notes.firstOrNull() ?: "新版本已可下载，更新后可获得最新体验。"
                            }
                            is UpdateOverlayState.Downloading -> "请保持网络连接，下载完成后将打开系统安装器。"
                            is UpdateOverlayState.Failed -> state.message
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = StkColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                    if (state is UpdateOverlayState.Downloading) {
                        Text(
                            formatProgress(state.progress),
                            style = MaterialTheme.typography.bodySmall,
                            color = StkColors.TextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LinearProgressIndicator(
                            progress = { (state.progress.percent ?: 0) / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = StkColors.BrandPrimary,
                            trackColor = StkColors.Border,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceSm),
                        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
                    ) {
                        if (!forced || state is UpdateOverlayState.Failed) {
                            OutlinedButton(
                                onClick = when {
                                    state is UpdateOverlayState.Downloading -> onCancelDownload ?: {}
                                    forced -> onContactSupport ?: {}
                                    else -> onLater ?: {}
                                },
                                enabled = !forced || onContactSupport != null,
                                modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight),
                                shape = RoundedCornerShape(StkDimens.RadiusControl),
                            ) {
                                Text(
                                    when {
                                        state is UpdateOverlayState.Downloading -> "取消"
                                        forced -> "联系客服"
                                        else -> "稍后"
                                    },
                                )
                            }
                        }
                        Button(
                            onClick = onOpenUpdate,
                            enabled = state !is UpdateOverlayState.Downloading,
                            modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight),
                            shape = RoundedCornerShape(StkDimens.RadiusControl),
                            colors = ButtonDefaults.buttonColors(containerColor = StkColors.BrandPrimary),
                        ) {
                            Text(if (state is UpdateOverlayState.Failed) "重新下载" else if (state is UpdateOverlayState.Downloading) "下载中" else "立即更新")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckingUpdateDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth(StkDimens.DialogWidthFraction),
                shape = RoundedCornerShape(StkDimens.RadiusDialog),
                color = StkColors.Surface,
            ) {
                Column(
                    modifier = Modifier.padding(StkDimens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase),
                ) {
                    StatusIcon(Icons.Default.Download, StkColors.BrandPrimary, compact = true)
                    Text("正在检查更新", style = MaterialTheme.typography.titleLarge)
                    Text("正在验证服务器发布配置。", color = StkColors.TextSecondary, textAlign = TextAlign.Center)
                    StkPrimaryButton("检查中", {}, enabled = false, loading = true)
                }
            }
        }
    }
}

@Composable
fun ReleaseUpdateScreen(
    releaseRepository: ReleaseRepository,
    initialRelease: ReleaseManifest? = null,
    initialForced: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val updateManager = remember(context) { ApkUpdateManager(context) }
    val updateViewModel: ReleaseUpdateViewModel = viewModel(
        key = "release-update-${initialRelease?.versionCode ?: "check"}-$initialForced",
        factory = ReleaseUpdateViewModel.Factory(
            releaseRepository = releaseRepository,
            updateManager = updateManager,
            installedVersionCode = BuildConfig.VERSION_CODE,
            initialRelease = initialRelease,
            initialForced = initialForced,
        ),
    )
    val state by updateViewModel.state.collectAsStateWithLifecycle()
    val forced = state.forcedOr(initialForced)
    BackHandler(enabled = forced) {}
    Column(Modifier.fillMaxSize().background(StkColors.Background)) {
        StkTopBar(
            title = "版本更新",
            navigation = if (!forced && onBack != null) {
                { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            } else {
                null
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(StkDimens.SpaceBase),
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceLg),
        ) {
            ReleaseAppCard(state.releaseOrNull())
            when (val current = state) {
                ReleaseUpdateUiState.Checking -> CheckingCard()
                is ReleaseUpdateUiState.CheckFailed -> {
                    StkFeedbackBanner(
                        message = current.message,
                        tone = StkFeedbackTone.Error,
                        actionLabel = "重试",
                        onAction = updateViewModel::check,
                    )
                    current.requestId?.let { Text("请求编号：$it", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall) }
                }
                is ReleaseUpdateUiState.UpToDate -> UpToDateContent(current, updateViewModel::check)
                is ReleaseUpdateUiState.Available -> AvailableContent(current.release, current.forced, updateViewModel::download)
                is ReleaseUpdateUiState.Downloading -> DownloadingContent(current, updateViewModel::cancelDownload)
                is ReleaseUpdateUiState.DownloadFailed -> {
                    ReleaseDetailsCard(current.release, current.forced)
                    StkFeedbackBanner(current.message, StkFeedbackTone.Error)
                    StkPrimaryButton("重新下载", updateViewModel::download)
                }
                is ReleaseUpdateUiState.ReadyToInstall -> {
                    ReleaseDetailsCard(current.release, current.forced)
                    StkFeedbackBanner("SHA-256 校验通过，安装包可以安装。", StkFeedbackTone.Success)
                    StkPrimaryButton("打开系统安装器", updateViewModel::install)
                }
                is ReleaseUpdateUiState.InstallBlocked -> {
                    ReleaseDetailsCard(current.release, current.forced)
                    StkFeedbackBanner(current.message, StkFeedbackTone.Error)
                    StkPrimaryButton(
                        if (current.permissionRequired) "已授权，继续安装" else "再次打开系统安装器",
                        updateViewModel::install,
                    )
                }
                is ReleaseUpdateUiState.InstallStarted -> {
                    ReleaseDetailsCard(current.release, current.forced)
                    StkFeedbackBanner("已打开系统安装器，请核对应用名称后继续。", StkFeedbackTone.Success)
                    StkPrimaryButton("再次打开系统安装器", updateViewModel::install)
                }
            }
        }
    }
}

@Composable
private fun ReleaseAppCard(release: ReleaseManifest?) {
    val host = release?.apkUrl?.let { runCatching { URL(it).host }.getOrNull() }
        ?: "stk-download.zz-yihao.com"
    StkInfoCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase)) {
            StkBrandMark(markSizeOverride = StkDimens.AboutBrandMark, iconSizeOverride = StkDimens.MinTouch)
            Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs)) {
                Text("商推客", style = MaterialTheme.typography.titleLarge)
                Text("当前版本 ${BuildConfig.VERSION_NAME}", color = StkColors.TextSecondary)
                Text("安装包来源：$host", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CheckingCard() {
    StkInfoCard(Modifier.height(StkDimens.UpdateStatusCardHeight)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase)) {
            CircularProgressIndicator(color = StkColors.BrandPrimary, strokeWidth = StkDimens.SplashProgressStroke)
            Text("正在检查版本", style = MaterialTheme.typography.titleLarge)
            Text("验证发布配置与下载域名", color = StkColors.TextSecondary)
        }
    }
}

@Composable
private fun UpToDateContent(state: ReleaseUpdateUiState.UpToDate, onCheck: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase)) {
        StatusIcon(Icons.Default.CheckCircleOutline, StkColors.Success)
        Text("已是最新版", style = MaterialTheme.typography.headlineLarge)
        Text(
            "当前版本 ${BuildConfig.VERSION_NAME} · 检查时间 ${formatCheckTime(state.checkedAtMillis)}",
            color = StkColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        StkPrimaryButton("重新检查", onCheck, Modifier.width(StkDimens.UpdatePrimaryButtonWidth))
    }
}

@Composable
private fun AvailableContent(release: ReleaseManifest, forced: Boolean, onDownload: () -> Unit) {
    ReleaseDetailsCard(release, forced)
    if (forced) {
        StkFeedbackBanner("当前版本低于最低可用版本，必须更新后继续。", StkFeedbackTone.Warning)
    }
    StkPrimaryButton(if (forced) "立即更新" else "下载并更新", onDownload)
}

@Composable
private fun DownloadingContent(state: ReleaseUpdateUiState.Downloading, onCancel: () -> Unit) {
    ReleaseDetailsCard(state.release, state.forced)
    Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
        Text("正在下载 ${formatProgress(state.progress)}", color = StkColors.TextSecondary)
        LinearProgressIndicator(
            progress = { (state.progress.percent ?: 0) / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = StkColors.BrandPrimary,
            trackColor = StkColors.Border,
        )
        Text(state.progress.percent?.let { "$it%" } ?: "正在接收安装包", color = StkColors.BrandPrimary, modifier = Modifier.align(Alignment.End))
    }
    StkPrimaryButton("下载中", {}, enabled = false)
    if (!state.forced) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(StkDimens.PrimaryControlHeight)) {
            Text("取消下载")
        }
    }
}

@Composable
private fun ReleaseDetailsCard(release: ReleaseManifest, forced: Boolean) {
    StkInfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("新版本 ${release.versionName}", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                Surface(
                    color = if (forced) StkColors.BrandAccentSoft else StkColors.BrandPrimarySoft,
                    shape = RoundedCornerShape(StkDimens.RadiusPill),
                ) {
                    Text(
                        if (forced) "强制更新" else "可选更新",
                        color = if (forced) StkColors.Warning else StkColors.BrandPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = StkDimens.SpaceMd, vertical = StkDimens.SpaceXs),
                    )
                }
            }
            release.apkSizeBytes?.let { Text("安装包大小 ${formatBytes(it)}", color = StkColors.TextSecondary) }
            Text("更新内容", style = MaterialTheme.typography.titleMedium)
            if (release.notes.isEmpty()) {
                Text("本次更新包含稳定性与安全改进。", color = StkColors.TextSecondary)
            } else {
                release.notes.forEach { Text("· $it", color = StkColors.TextSecondary) }
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, tint: Color, compact: Boolean = false) {
    Box(
        Modifier
            .size(if (compact) StkDimens.MinTouch else StkDimens.SystemFeedbackIcon)
            .background(tint.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(if (compact) StkDimens.Icon else StkDimens.SystemFeedbackInnerIcon))
    }
}

private fun ReleaseUpdateUiState.releaseOrNull(): ReleaseManifest? = when (this) {
    ReleaseUpdateUiState.Checking, is ReleaseUpdateUiState.CheckFailed -> null
    is ReleaseUpdateUiState.UpToDate -> release
    is ReleaseUpdateUiState.Available -> release
    is ReleaseUpdateUiState.Downloading -> release
    is ReleaseUpdateUiState.DownloadFailed -> release
    is ReleaseUpdateUiState.ReadyToInstall -> release
    is ReleaseUpdateUiState.InstallBlocked -> release
    is ReleaseUpdateUiState.InstallStarted -> release
}

private fun ReleaseUpdateUiState.forcedOr(default: Boolean): Boolean = when (this) {
    is ReleaseUpdateUiState.Available -> forced
    is ReleaseUpdateUiState.Downloading -> forced
    is ReleaseUpdateUiState.DownloadFailed -> forced
    is ReleaseUpdateUiState.ReadyToInstall -> forced
    is ReleaseUpdateUiState.InstallBlocked -> forced
    is ReleaseUpdateUiState.InstallStarted -> forced
    else -> default
}

private fun feedbackContent(state: SystemFeedbackState, release: ReleaseManifest?, message: String?): FeedbackContent = when (state) {
    SystemFeedbackState.UpToDate -> FeedbackContent("已是最新版本", message ?: "当前已安装商推客 ${release?.versionName ?: BuildConfig.VERSION_NAME}。", Icons.Default.CheckCircleOutline, StkColors.Success)
    SystemFeedbackState.OptionalUpdate -> FeedbackContent("发现新版本", message ?: "新版本已可下载。", Icons.Default.SystemUpdate, StkColors.BrandPrimary)
    SystemFeedbackState.ForcedUpdate -> FeedbackContent("需要更新后继续使用", message ?: "当前版本低于最低可用版本。", Icons.Default.WarningAmber, StkColors.Warning)
    SystemFeedbackState.Downloading -> FeedbackContent("正在下载更新", message ?: "请保持网络连接。", Icons.Default.Download, StkColors.BrandPrimary)
    SystemFeedbackState.InstallBlocked -> FeedbackContent("安装受阻", message ?: "请检查系统安装权限后重试。", Icons.Default.ErrorOutline, StkColors.Error)
    SystemFeedbackState.Maintenance -> FeedbackContent("系统维护中", message ?: "后台正在进行服务维护，请稍后再试。", Icons.Default.WarningAmber, StkColors.Warning)
    SystemFeedbackState.ServiceRecovered -> FeedbackContent("服务已恢复", message ?: "连接已经恢复，可以继续使用商推客。", Icons.Default.CheckCircleOutline, StkColors.Success)
    SystemFeedbackState.Offline -> FeedbackContent("暂时无法连接服务", message ?: "请检查网络连接后重试。登录状态仍安全保存在本机。", Icons.Default.CloudOff, StkColors.Error)
    SystemFeedbackState.SystemError -> FeedbackContent("系统出现异常", message ?: "服务请求未完成，请使用请求编号联系管理员。", Icons.Default.ErrorOutline, StkColors.Error)
}

private fun formatProgress(progress: DownloadProgress): String {
    val total = progress.totalBytes?.let(::formatBytes)
    return if (total == null) formatBytes(progress.downloadedBytes) else "${formatBytes(progress.downloadedBytes)} / $total"
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}

private fun formatCheckTime(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(timestamp))

private data class FeedbackContent(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val tint: Color,
)
