package com.zzyihao.stk.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.zzyihao.stk.ui.components.StkBrandMark
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

enum class StartupVisualState { Checking, NeedsLogin, Ready, Offline, Error }

@Composable
fun SplashScreen(
    state: StartupVisualState = StartupVisualState.Checking,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    actionLabel: String = "重新加载",
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StkColors.Surface),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = StkColors.BrandPrimarySoft,
                radius = size.width * 0.31f,
                center = Offset(size.width * 0.87f, size.height * 0.06f),
            )
            drawCircle(
                color = StkColors.BrandAccentSoft,
                radius = size.width * 0.49f,
                center = Offset(size.width * 0.08f, size.height * 0.90f),
            )
        }
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = StkDimens.SplashBrandTop),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StkBrandMark(
                markSizeOverride = StkDimens.SplashBrandSize,
                iconSizeOverride = StkDimens.SplashBrandIcon,
            )
            Spacer(Modifier.height(StkDimens.Space2Xl))
            Text("商推客", style = MaterialTheme.typography.headlineLarge, color = StkColors.TextPrimary)
            Text("发现项目 · 高效推广", style = MaterialTheme.typography.titleMedium, color = StkColors.TextSecondary)
            Spacer(Modifier.height(StkDimens.Space2Xl + StkDimens.SpaceLg))
            StartupStateIcon(state)
            Spacer(Modifier.height(StkDimens.SpaceBase))
            Text(
                when (state) {
                    StartupVisualState.Checking -> "正在准备商推客"
                    StartupVisualState.NeedsLogin -> "需要登录"
                    StartupVisualState.Ready -> "准备就绪"
                    StartupVisualState.Offline -> "当前处于离线状态"
                    StartupVisualState.Error -> "启动配置加载失败"
                },
                style = MaterialTheme.typography.titleLarge,
                color = StkColors.TextPrimary,
            )
            Text(
                message ?: when (state) {
                    StartupVisualState.Checking -> "检查配置、登录状态与版本信息"
                    StartupVisualState.NeedsLogin -> "正在进入手机号登录页面"
                    StartupVisualState.Ready -> "正在进入已缓存的首页内容"
                    StartupVisualState.Offline -> "可继续查看最近缓存的项目内容"
                    StartupVisualState.Error -> "没有出现白屏，您可以重新尝试"
                },
                style = MaterialTheme.typography.bodySmall,
                color = StkColors.TextTertiary,
            )
            if (state in setOf(StartupVisualState.Error, StartupVisualState.Offline) && onRetry != null) {
                StkPrimaryButton(
                    text = actionLabel,
                    onClick = onRetry,
                    modifier = Modifier.width(StkDimens.EmptyStateButtonWidth).padding(top = StkDimens.SpaceXl),
                )
            }
        }
        Text(
            text = "商推客 · Android",
            style = MaterialTheme.typography.bodySmall,
            color = StkColors.TextTertiary,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = StkDimens.Space2Xl),
        )
    }
}

@Composable
private fun StartupStateIcon(state: StartupVisualState) {
    if (state == StartupVisualState.Checking) {
        CircularProgressIndicator(
            modifier = Modifier.size(StkDimens.SplashProgressSize),
            color = StkColors.BrandPrimary,
            strokeWidth = StkDimens.SplashProgressStroke,
        )
        return
    }
    val tint = when (state) {
        StartupVisualState.NeedsLogin -> StkColors.BrandPrimary
        StartupVisualState.Ready -> StkColors.Success
        StartupVisualState.Offline -> StkColors.Warning
        StartupVisualState.Error -> StkColors.Error
        StartupVisualState.Checking -> StkColors.BrandPrimary
    }
    val icon = when (state) {
        StartupVisualState.NeedsLogin -> Icons.Outlined.LockOpen
        StartupVisualState.Ready -> Icons.Outlined.Check
        StartupVisualState.Offline -> Icons.Outlined.CloudOff
        StartupVisualState.Error -> Icons.Outlined.ErrorOutline
        StartupVisualState.Checking -> Icons.Outlined.Check
    }
    Box(
        modifier = Modifier.size(StkDimens.MinTouch).background(tint.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(StkDimens.Icon))
    }
}
