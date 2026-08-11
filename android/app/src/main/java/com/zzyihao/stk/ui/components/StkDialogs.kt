package com.zzyihao.stk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

enum class StkStatusKind { Info, Warning, Error, Security, Time, Session }

@Composable
fun StkStatusDialog(
    title: String,
    message: String,
    kind: StkStatusKind,
    buttonLabel: String,
    onDismiss: () -> Unit,
) {
    val tint = when (kind) {
        StkStatusKind.Info -> StkColors.BrandPrimary
        StkStatusKind.Warning, StkStatusKind.Time, StkStatusKind.Session -> StkColors.Warning
        StkStatusKind.Error, StkStatusKind.Security -> StkColors.Error
    }
    val soft = when (kind) {
        StkStatusKind.Info -> StkColors.BrandPrimarySoft
        StkStatusKind.Warning, StkStatusKind.Time, StkStatusKind.Session -> StkColors.BrandAccentSoft
        StkStatusKind.Error, StkStatusKind.Security -> StkColors.ErrorSoft
    }
    val icon = when (kind) {
        StkStatusKind.Info -> Icons.Outlined.Info
        StkStatusKind.Warning -> Icons.Outlined.Shield
        StkStatusKind.Error -> Icons.Outlined.ErrorOutline
        StkStatusKind.Security -> Icons.Outlined.Shield
        StkStatusKind.Time -> Icons.Outlined.AccessTime
        StkStatusKind.Session -> Icons.Outlined.LockOpen
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        DialogSurface {
            Box(
                Modifier.size(StkDimens.MinTouch).background(soft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(StkDimens.Icon))
            }
            Spacer(Modifier.height(StkDimens.SpaceLg))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = StkColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(StkDimens.SpaceMd))
            Text(
                message,
                style = if (kind == StkStatusKind.Time) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = StkColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(StkDimens.Space2Xl + StkDimens.SpaceLg))
            StkPrimaryButton(text = buttonLabel, onClick = onDismiss)
        }
    }
}

enum class LogoutDialogState { Confirm, Loading, Failed }

@Composable
fun StkLogoutDialog(
    state: LogoutDialogState = LogoutDialogState.Confirm,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val busy = state == LogoutDialogState.Loading
    Dialog(
        onDismissRequest = { if (state == LogoutDialogState.Confirm) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = state == LogoutDialogState.Confirm,
            dismissOnClickOutside = state == LogoutDialogState.Confirm,
        ),
    ) {
        DialogSurface {
            Box(
                Modifier.size(StkDimens.MinTouch).background(StkColors.BrandAccentSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (state == LogoutDialogState.Failed) Icons.Outlined.WarningAmber else Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = StkColors.Warning,
                    modifier = Modifier.size(StkDimens.Icon),
                )
            }
            Spacer(Modifier.height(StkDimens.SpaceLg))
            Text(
                when (state) {
                    LogoutDialogState.Confirm -> "退出当前账号？"
                    LogoutDialogState.Loading -> "正在退出登录"
                    LogoutDialogState.Failed -> "服务器撤销失败"
                },
                style = MaterialTheme.typography.titleLarge,
                color = StkColors.TextPrimary,
            )
            Spacer(Modifier.height(StkDimens.SpaceMd))
            Text(
                when (state) {
                    LogoutDialogState.Confirm -> "退出后将清除本机登录状态，缓存内容仍按规则保留。"
                    LogoutDialogState.Loading -> "正在撤销服务器令牌，请稍候。"
                    LogoutDialogState.Failed -> "已完成本地安全退出，并记录待撤销令牌。"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = StkColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(StkDimens.Space2Xl + StkDimens.SpaceLg))
            if (state == LogoutDialogState.Failed) {
                StkPrimaryButton(text = "返回登录", onClick = onConfirm)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                    DialogButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        container = StkColors.Surface,
                        content = StkColors.TextPrimary,
                        outlined = true,
                        enabled = !busy,
                    )
                    DialogButton(
                        text = if (busy) "退出中" else "确认退出",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        container = if (busy) StkColors.BrandPrimary else StkColors.Error,
                        content = StkColors.Surface,
                        enabled = !busy,
                        loading = busy,
                    )
                }
            }
        }
    }
}

@Composable
fun StkRegistrationSuccessDialog(
    loading: Boolean,
    onContinue: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        DialogSurface {
            Box(
                Modifier.size(StkDimens.MinTouch).background(StkColors.SuccessSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = StkColors.Success,
                    modifier = Modifier.size(StkDimens.Icon),
                )
            }
            Spacer(Modifier.height(StkDimens.SpaceLg))
            Text("注册成功", style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
            Spacer(Modifier.height(StkDimens.SpaceMd))
            Text(
                "账号已创建，并已生成 UID、会员状态与两个基础账户。",
                style = MaterialTheme.typography.bodyLarge,
                color = StkColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(StkDimens.Space2Xl))
            StkPrimaryButton(
                text = "进入商推客",
                onClick = onContinue,
                loading = loading,
            )
        }
    }
}

@Composable
private fun DialogSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(StkDimens.DialogWidthFraction)
            .widthIn(max = StkDimens.CaptchaDialogMaxWidth)
            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusDialog))
            .padding(StkDimens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    outlined: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(StkDimens.PrimaryControlHeight)
            .then(
                if (outlined) Modifier.border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl))
                else Modifier,
            ),
        shape = RoundedCornerShape(StkDimens.RadiusControl),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container,
            disabledContentColor = content,
        ),
    ) {
        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(StkDimens.InlineProgress),
                    color = content,
                    strokeWidth = StkDimens.SplashProgressStroke,
                )
                Text(text, style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
