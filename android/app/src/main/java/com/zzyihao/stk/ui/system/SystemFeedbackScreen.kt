package com.zzyihao.stk.ui.system

import com.zzyihao.stk.BuildConfig
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.zzyihao.stk.data.release.ReleaseManifest
import com.zzyihao.stk.data.release.ApkUpdateManager
import com.zzyihao.stk.data.release.InstallResult
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import java.io.File
import kotlinx.coroutines.launch

enum class SystemFeedbackState {
    UpToDate, OptionalUpdate, ForcedUpdate, Downloading, InstallBlocked, Maintenance, Offline, SystemError,
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
    val context = LocalContext.current
    val updateManager = remember(context) { ApkUpdateManager(context) }
    val scope = rememberCoroutineScope()
    var updateAction by remember(release?.versionCode) { mutableStateOf<UpdateActionState>(UpdateActionState.Idle) }
    val content = feedbackContent(initialState, release, message)
    val canOpenDownload = release?.apkUrl?.startsWith("https://") == true
    val openDownload: () -> Unit = {
        if (canOpenDownload) {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release!!.apkUrl))) }
        }
        Unit
    }
    val downloadAndInstall: () -> Unit = {
        val currentRelease = release
        if (currentRelease != null) {
            scope.launch {
                updateAction = UpdateActionState.Downloading(null)
                runCatching {
                    updateManager.download(currentRelease) { progress ->
                        scope.launch { updateAction = UpdateActionState.Downloading(progress) }
                    }
                }.onSuccess { file ->
                    updateAction = runCatching { updateManager.install(file) }
                        .fold(
                            onSuccess = { result -> if (result == InstallResult.Started) UpdateActionState.InstallStarted else UpdateActionState.PermissionRequired(file) },
                            onFailure = { UpdateActionState.Failed(it.message ?: "无法启动系统安装器") },
                        )
                }.onFailure { updateAction = UpdateActionState.Failed(it.message ?: "安装包下载失败") }
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        val isSystemState = initialState in setOf(
            SystemFeedbackState.Maintenance,
            SystemFeedbackState.Offline,
            SystemFeedbackState.SystemError,
        )
        StkTopBar(
            title = if (isSystemState) "系统状态" else "版本与服务",
            navigation = if (initialState == SystemFeedbackState.ForcedUpdate || isSystemState) null else onBack?.let { callback ->
                { IconButton(onClick = callback) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = StkDimens.SpaceBase,
                    top = StkDimens.SpaceBase,
                    end = StkDimens.SpaceBase,
                    bottom = if (isSystemState) StkDimens.SystemFeedbackBottomPadding else StkDimens.SpaceBase,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(StkDimens.SystemFeedbackIcon).background(content.tint.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(content.icon, contentDescription = null, tint = content.tint, modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon))
            }
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
            if (release != null && initialState in setOf(SystemFeedbackState.OptionalUpdate, SystemFeedbackState.ForcedUpdate)) {
                StkInfoCard(Modifier.fillMaxWidth().padding(top = StkDimens.SpaceXl)) {
                    Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                        Text("版本 ${release.versionName}", style = MaterialTheme.typography.titleMedium)
                        release.notes.forEach { Text("· $it", color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall) }
                        Text("安装包 SHA-256：${release.sha256}", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            when (initialState) {
                SystemFeedbackState.OptionalUpdate, SystemFeedbackState.ForcedUpdate -> {
                    when (val action = updateAction) {
                        is UpdateActionState.Downloading -> {
                            LinearProgressIndicator(
                                progress = { (action.percent ?: 0) / 100f },
                                modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceXl),
                                color = StkColors.BrandPrimary,
                                trackColor = StkColors.Border,
                            )
                            Text(action.percent?.let { "正在下载 $it%" } ?: "正在下载并校验安装包", color = StkColors.TextSecondary, modifier = Modifier.padding(top = StkDimens.SpaceSm))
                        }
                        is UpdateActionState.PermissionRequired -> StkPrimaryButton(
                            text = "已授权，继续安装",
                            onClick = {
                                updateAction = runCatching { updateManager.install(action.file) }
                                    .fold(
                                        onSuccess = { if (it == InstallResult.Started) UpdateActionState.InstallStarted else action },
                                        onFailure = { UpdateActionState.Failed(it.message ?: "无法启动系统安装器") },
                                    )
                            },
                            modifier = Modifier.padding(top = StkDimens.SpaceXl),
                        )
                        else -> StkPrimaryButton(
                            text = if (updateAction is UpdateActionState.Failed) "重新下载并安装" else "下载并安装",
                            onClick = downloadAndInstall,
                            enabled = canOpenDownload,
                            modifier = Modifier.padding(top = StkDimens.SpaceXl),
                        )
                    }
                    when (val action = updateAction) {
                        is UpdateActionState.Failed -> Text(action.message, color = StkColors.Error, modifier = Modifier.padding(top = StkDimens.SpaceSm))
                        UpdateActionState.InstallStarted -> Text("已打开系统安装器，请核对应用名称后继续。", color = StkColors.Success, modifier = Modifier.padding(top = StkDimens.SpaceSm))
                        is UpdateActionState.PermissionRequired -> Text("请在系统页面允许安装未知应用，然后返回继续。", color = StkColors.Warning, modifier = Modifier.padding(top = StkDimens.SpaceSm))
                        else -> Unit
                    }
                    TextButton(onClick = openDownload, enabled = canOpenDownload) { Text("使用浏览器下载") }
                }
                SystemFeedbackState.Maintenance -> onRetry?.let {
                    StkPrimaryButton("重新连接", it, Modifier.padding(top = StkDimens.SpaceXl).width(StkDimens.SystemFeedbackButtonWidth))
                }
                SystemFeedbackState.InstallBlocked -> onRetry?.let {
                    StkPrimaryButton("重试", it, Modifier.padding(top = StkDimens.SpaceXl).width(StkDimens.SystemFeedbackButtonWidth))
                }
                SystemFeedbackState.Offline -> onRetry?.let {
                    StkPrimaryButton("重新连接", it, Modifier.padding(top = StkDimens.SpaceXl).width(StkDimens.SystemFeedbackButtonWidth))
                }
                SystemFeedbackState.SystemError -> onRetry?.let {
                    StkPrimaryButton("重新尝试", it, Modifier.padding(top = StkDimens.SpaceXl).width(StkDimens.SystemFeedbackButtonWidth))
                }
                else -> Unit
            }
            if (isSystemState && onBack != null) {
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
            if (initialState == SystemFeedbackState.OptionalUpdate && onBack != null) {
                TextButton(onClick = onBack) { Text("暂不更新") }
            }
        }
    }
}

private sealed interface UpdateActionState {
    data object Idle : UpdateActionState
    data class Downloading(val percent: Int?) : UpdateActionState
    data class PermissionRequired(val file: File) : UpdateActionState
    data class Failed(val message: String) : UpdateActionState
    data object InstallStarted : UpdateActionState
}

private fun feedbackContent(state: SystemFeedbackState, release: ReleaseManifest?, message: String?): FeedbackContent = when (state) {
    SystemFeedbackState.UpToDate -> FeedbackContent("已是最新版本", "当前已安装商推客 ${release?.versionName ?: BuildConfig.VERSION_NAME}。", Icons.Default.SystemUpdate, StkColors.BrandPrimary)
    SystemFeedbackState.OptionalUpdate -> FeedbackContent("发现新版本", message ?: "${release?.versionName ?: "新版本"} 已可下载，更新可获得最新功能。", Icons.Default.SystemUpdate, StkColors.BrandPrimary)
    SystemFeedbackState.ForcedUpdate -> FeedbackContent("需要更新后才能继续", message ?: "当前版本低于服务要求，请从官方页面下载并核对校验值。", Icons.Default.WarningAmber, StkColors.Warning)
    SystemFeedbackState.Downloading -> FeedbackContent("已转到下载页", "请在浏览器完成下载，并在安装前核对 SHA-256。", Icons.Default.SystemUpdate, StkColors.BrandPrimary)
    SystemFeedbackState.InstallBlocked -> FeedbackContent("安装受阻", "请检查系统安装权限后重试。", Icons.Default.ErrorOutline, StkColors.Error)
    SystemFeedbackState.Maintenance -> FeedbackContent("服务暂不可用", message ?: "服务器暂时无法响应，请检查网络后重试。", Icons.Default.ErrorOutline, StkColors.Error)
    SystemFeedbackState.Offline -> FeedbackContent("暂时无法连接服务", message ?: "请检查网络连接后重试。登录状态仍安全保存在本机。", Icons.Default.ErrorOutline, StkColors.Error)
    SystemFeedbackState.SystemError -> FeedbackContent("系统出现异常", message ?: "服务请求未完成，请使用请求编号联系管理员。", Icons.Default.ErrorOutline, StkColors.Error)
}

private data class FeedbackContent(val title: String, val message: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: androidx.compose.ui.graphics.Color)
