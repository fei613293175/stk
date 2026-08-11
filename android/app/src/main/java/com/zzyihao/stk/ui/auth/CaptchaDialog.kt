package com.zzyihao.stk.ui.auth

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.zzyihao.stk.data.auth.AuthValidators
import com.zzyihao.stk.data.auth.CaptchaChallenge
import com.zzyihao.stk.ui.components.StkTextField
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

@Composable
fun CaptchaDialog(
    challenge: CaptchaChallenge?,
    operation: AuthOperationState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val challengeKey = challenge?.challengeId ?: "loading"
    var code by rememberSaveable(challengeKey) { mutableStateOf("") }
    var submitted by rememberSaveable(challengeKey) { mutableStateOf(false) }
    var imageLoaded by rememberSaveable(challengeKey, challenge?.imageUrl) {
        mutableStateOf(challenge != null && challenge.imageUrl == null)
    }
    var imageFailed by rememberSaveable(challengeKey, challenge?.imageUrl) { mutableStateOf(false) }
    val challengeLoading = operation.operation == AuthOperation.CaptchaLoading && challenge == null
    val refreshing = operation.operation == AuthOperation.CaptchaRefreshing
    val validating = operation.operation in setOf(
        AuthOperation.PasswordLogin,
        AuthOperation.SmsSending,
        AuthOperation.SmsLogin,
        AuthOperation.Registering,
    ) && operation.captchaVisible
    val verified = operation.operation == AuthOperation.CaptchaSuccess
    val expired = operation.operation == AuthOperation.CaptchaExpired
    val networkFailed = operation.isError &&
        operation.issue in setOf(AuthIssue.Network, AuthIssue.Timeout) &&
        operation.captchaVisible
    val busy = challengeLoading || refreshing || validating || verified
    val localError = if (submitted || code.isNotBlank()) AuthValidators.captchaCodeError(code) else null
    val fieldError = localError ?: operation.message.takeIf {
        operation.errorCode == 4002 || expired
    }

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !busy,
            dismissOnClickOutside = !busy,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(StkDimens.DialogWidthFraction)
                .widthIn(max = StkDimens.CaptchaDialogMaxWidth)
                .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusDialog))
                .padding(StkDimens.SpaceXl),
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
        ) {
            Text("安全验证", style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
            Text(
                "完成验证后才会执行当前操作",
                style = MaterialTheme.typography.bodyLarge,
                color = StkColors.TextSecondary,
            )
            CaptchaImagePanel(
                challenge = challenge,
                challengeLoading = challengeLoading,
                refreshing = refreshing,
                networkFailed = networkFailed,
                onImageLoaded = { imageLoaded = true; imageFailed = false },
                onImageFailed = { imageLoaded = false; imageFailed = true },
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("验证码", style = MaterialTheme.typography.titleMedium, color = StkColors.TextSecondary)
                TextButton(
                    onClick = {
                        code = ""
                        submitted = false
                        imageLoaded = false
                        imageFailed = false
                        onRefresh()
                    },
                    enabled = !busy,
                ) {
                    Text("刷新", color = StkColors.BrandPrimary)
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "刷新验证码",
                        tint = StkColors.BrandPrimary,
                        modifier = Modifier.padding(start = StkDimens.SpaceXs),
                    )
                }
            }
            StkTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(4) },
                label = "请输入图中字符",
                error = fieldError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = challenge != null && !busy && !networkFailed,
            )
            if (imageFailed && !networkFailed) {
                Text(
                    "验证码图片未加载，刷新后再试",
                    color = StkColors.Error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                Button(
                    onClick = onDismiss,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(StkDimens.PrimaryControlHeight)
                        .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StkColors.Surface,
                        contentColor = StkColors.TextPrimary,
                    ),
                    shape = RoundedCornerShape(StkDimens.RadiusControl),
                ) { Text("取消", style = MaterialTheme.typography.titleMedium) }
                Button(
                    onClick = {
                        if (networkFailed) {
                            onRefresh()
                        } else {
                            submitted = true
                            if (AuthValidators.captchaCodeError(code) == null) onConfirm(code.trim())
                        }
                    },
                    enabled = when {
                        networkFailed -> true
                        busy -> false
                        else -> challenge != null && imageLoaded && !imageFailed
                    },
                    modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StkColors.BrandPrimary,
                        disabledContainerColor = StkColors.BrandPrimary,
                        disabledContentColor = StkColors.Surface,
                    ),
                    shape = RoundedCornerShape(StkDimens.RadiusControl),
                ) {
                    when {
                        validating -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(StkDimens.InlineProgress),
                                color = StkColors.Surface,
                                strokeWidth = StkDimens.SplashProgressStroke,
                            )
                            Text("验证中", style = MaterialTheme.typography.titleMedium)
                        }
                        verified -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(StkDimens.IconSmall))
                            Text("验证通过", style = MaterialTheme.typography.titleMedium)
                        }
                        networkFailed -> Text("重新加载", style = MaterialTheme.typography.titleMedium)
                        else -> Text("确认", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptchaImagePanel(
    challenge: CaptchaChallenge?,
    challengeLoading: Boolean,
    refreshing: Boolean,
    networkFailed: Boolean,
    onImageLoaded: () -> Unit,
    onImageFailed: () -> Unit,
) {
    val decodedImage = remember(challenge?.challengeId) {
        challenge?.imageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    val panelColor = when {
        networkFailed -> StkColors.ErrorSoft
        else -> StkColors.Background
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(StkDimens.CaptchaImageHeight)
            .background(panelColor, RoundedCornerShape(StkDimens.RadiusControl))
            .then(
                if (networkFailed) {
                    Modifier.border(StkDimens.Divider, StkColors.Error, RoundedCornerShape(StkDimens.RadiusControl))
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            networkFailed -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
            ) {
                Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = StkColors.Error)
                Text("验证码加载失败", color = StkColors.Error, style = MaterialTheme.typography.titleMedium)
            }
            challengeLoading || refreshing -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
            ) {
                CircularProgressIndicator(
                    color = StkColors.BrandPrimary,
                    modifier = Modifier.size(StkDimens.Icon),
                    strokeWidth = StkDimens.SplashProgressStroke,
                )
                if (refreshing) {
                    Text("正在刷新验证码", color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            decodedImage != null -> {
                LaunchedEffect(challenge?.challengeId) { onImageLoaded() }
                Image(
                    bitmap = decodedImage,
                    contentDescription = "图形验证码",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StkDimens.CaptchaImageHeight)
                        .clip(RoundedCornerShape(StkDimens.RadiusControl)),
                )
            }
            challenge?.imageUrl != null -> key(challenge.challengeId) {
                SubcomposeAsyncImage(
                    model = challenge.imageUrl,
                    contentDescription = "图形验证码",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StkDimens.CaptchaImageHeight)
                        .clip(RoundedCornerShape(StkDimens.RadiusControl)),
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Success -> {
                            LaunchedEffect(challenge.challengeId, challenge.imageUrl) { onImageLoaded() }
                            SubcomposeAsyncImageContent()
                        }
                        is AsyncImagePainter.State.Error -> {
                            LaunchedEffect(challenge.challengeId, challenge.imageUrl) { onImageFailed() }
                            Text("验证码加载失败，请刷新", color = StkColors.Error)
                        }
                        else -> CircularProgressIndicator(
                            color = StkColors.BrandPrimary,
                            modifier = Modifier.size(StkDimens.Icon),
                            strokeWidth = StkDimens.SplashProgressStroke,
                        )
                    }
                }
            }
            challenge != null -> Text(
                text = challenge.prompt,
                style = MaterialTheme.typography.headlineLarge,
                color = StkColors.BrandPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
