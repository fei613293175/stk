package com.zzyihao.stk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

enum class StkFeedbackTone { Success, Warning, Error }

@Composable
fun StkConfirmDialog(
    title: String,
    message: String,
    primaryLabel: String,
    secondaryLabel: String,
    icon: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tone: StkFeedbackTone = StkFeedbackTone.Error,
    preview: (@Composable () -> Unit)? = null,
) {
    val accent = when (tone) {
        StkFeedbackTone.Success -> StkColors.Success
        StkFeedbackTone.Warning -> StkColors.Warning
        StkFeedbackTone.Error -> StkColors.Error
    }
    val iconBackground = when (tone) {
        StkFeedbackTone.Success -> StkColors.SuccessSoft
        StkFeedbackTone.Warning -> StkColors.BrandAccentSoft
        StkFeedbackTone.Error -> StkColors.ErrorSoft
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Box(
                        modifier = Modifier
                            .size(StkDimens.MinTouch)
                            .background(iconBackground, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(StkDimens.Icon))
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary, textAlign = TextAlign.Center)
                    preview?.invoke()
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary, textAlign = TextAlign.Center)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceSm),
                        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight),
                            shape = RoundedCornerShape(StkDimens.RadiusControl),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StkColors.TextSecondary),
                        ) { Text(secondaryLabel) }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight),
                            shape = RoundedCornerShape(StkDimens.RadiusControl),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                        ) { Text(primaryLabel, color = StkColors.Surface) }
                    }
                }
            }
        }
    }
}

@Composable
fun StkFeedbackBanner(
    message: String,
    tone: StkFeedbackTone,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    emphasized: Boolean = false,
) {
    val accent = when (tone) {
        StkFeedbackTone.Success -> StkColors.Success
        StkFeedbackTone.Warning -> StkColors.Warning
        StkFeedbackTone.Error -> StkColors.Error
    }
    val container = if (emphasized) {
        accent
    } else {
        when (tone) {
            StkFeedbackTone.Success -> StkColors.SuccessSoft
            StkFeedbackTone.Warning -> StkColors.BrandAccentSoft
            StkFeedbackTone.Error -> StkColors.ErrorSoft
        }
    }
    val contentColor = if (emphasized) StkColors.Surface else StkColors.TextSecondary
    val icon = when (tone) {
        StkFeedbackTone.Success -> Icons.Outlined.CheckCircle
        StkFeedbackTone.Warning -> Icons.Outlined.WarningAmber
        StkFeedbackTone.Error -> Icons.Outlined.ErrorOutline
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = StkDimens.FeedbackMinHeight)
            .background(container, RoundedCornerShape(StkDimens.RadiusControl))
            .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
    ) {
        if (!emphasized) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(StkDimens.IconSmall))
        }
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                modifier = Modifier.clickable(onClick = onAction).padding(StkDimens.SpaceSm),
                style = MaterialTheme.typography.titleMedium,
                color = if (emphasized) StkColors.Surface else accent,
            )
        }
    }
}

@Composable
fun StkPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(StkDimens.PrimaryControlHeight),
        shape = RoundedCornerShape(StkDimens.RadiusControl),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) StkColors.BrandPrimaryPressed else StkColors.BrandPrimary,
            disabledContainerColor = StkColors.Disabled,
        ),
        interactionSource = interactionSource,
    ) {
        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(StkDimens.InlineProgress),
                    color = StkColors.Surface,
                    strokeWidth = StkDimens.SplashProgressStroke,
                )
                loadingText?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            }
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StkBrandMark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    markSizeOverride: androidx.compose.ui.unit.Dp? = null,
    iconSizeOverride: androidx.compose.ui.unit.Dp? = null,
) {
    val markSize = markSizeOverride ?: if (compact) StkDimens.AppBrandSize else StkDimens.AuthBrandSize
    val iconSize = iconSizeOverride ?: if (compact) StkDimens.AppBrandIcon else StkDimens.AuthBrandIcon
    Box(
        modifier = modifier
            .size(markSize)
            .background(
                Brush.linearGradient(listOf(StkColors.BrandMarkStart, StkColors.BrandMarkEnd)),
                RoundedCornerShape(if (compact) StkDimens.RadiusControl else StkDimens.RadiusDialog),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // The reference mark is a white rising arrow with an orange endpoint;
        // draw it directly so the brand silhouette does not depend on a generic icon.
        Canvas(Modifier.size(iconSize)) {
            val path = Path().apply {
                moveTo(size.width * .10f, size.height * .78f)
                lineTo(size.width * .43f, size.height * .48f)
                lineTo(size.width * .63f, size.height * .64f)
                lineTo(size.width * .90f, size.height * .22f)
            }
            drawPath(path, color = StkColors.Surface, style = Stroke(width = size.minDimension * .14f))
            drawLine(
                color = StkColors.Surface,
                start = androidx.compose.ui.geometry.Offset(size.width * .68f, size.height * .22f),
                end = androidx.compose.ui.geometry.Offset(size.width * .90f, size.height * .22f),
                strokeWidth = size.minDimension * .14f,
            )
            drawLine(
                color = StkColors.Surface,
                start = androidx.compose.ui.geometry.Offset(size.width * .90f, size.height * .22f),
                end = androidx.compose.ui.geometry.Offset(size.width * .90f, size.height * .45f),
                strokeWidth = size.minDimension * .14f,
            )
            drawCircle(StkColors.BrandAccent, radius = size.minDimension * .14f, center = androidx.compose.ui.geometry.Offset(size.width * .88f, size.height * .12f))
        }
    }
}

@Composable
fun StkAppHeader(
    modifier: Modifier = Modifier,
    title: String = "商推客",
    onProfile: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(StkDimens.TopBarHeight)
            .background(StkColors.Surface)
            .padding(horizontal = StkDimens.SpaceBase),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
    ) {
        StkBrandMark(compact = true)
        Text(title, style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
        Spacer(Modifier.weight(1f))
        onSearch?.let { callback ->
            IconButton(onClick = callback) { Icon(Icons.Default.Search, contentDescription = "搜索", tint = StkColors.TextSecondary) }
        }
        onRefresh?.let { callback ->
            IconButton(onClick = callback, enabled = !refreshing) {
                if (refreshing) CircularProgressIndicator(color = StkColors.BrandPrimary, strokeWidth = StkDimens.Divider)
                else Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = StkColors.TextSecondary)
            }
        }
        Box(
            modifier = Modifier
                .size(StkDimens.AppBrandSize)
                .background(StkColors.BrandPrimarySoft, androidx.compose.foundation.shape.CircleShape)
                .then(if (onProfile != null) Modifier.clickable { onProfile() } else Modifier),
            contentAlignment = Alignment.Center,
        ) { Text("商", style = MaterialTheme.typography.titleMedium, color = StkColors.BrandPrimary) }
    }
}

@Composable
fun StkSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabledItems: List<Boolean> = List(items.size) { true },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(StkDimens.SegmentHeight)
            .background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusDialog))
            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusDialog))
            .padding(StkDimens.SpaceXs),
    ) {
        items.forEachIndexed { index, item ->
            val active = index == selectedIndex
            val enabled = enabledItems.getOrElse(index) { true }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        if (active) StkColors.Surface else Color.Transparent,
                        RoundedCornerShape(StkDimens.RadiusControl),
                    )
                    .border(
                        if (active) StkDimens.Divider else 0.dp,
                        if (active) StkColors.Border else Color.Transparent,
                        RoundedCornerShape(StkDimens.RadiusControl),
                    )
                    .clickable(enabled = enabled) { onSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        active -> StkColors.BrandPrimary
                        enabled -> StkColors.TextSecondary
                        else -> StkColors.Disabled
                    },
                )
            }
        }
    }
}

@Composable
fun StkFormLabel(text: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary)
        Spacer(Modifier.weight(1f))
        trailing?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = StkColors.TextTertiary) }
    }
}

@Composable
fun StkPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(StkDimens.SmallButtonHeight)
            .background(if (selected) StkColors.BrandPrimary else StkColors.Surface, RoundedCornerShape(StkDimens.RadiusPill))
            .border(StkDimens.Divider, if (selected) StkColors.BrandPrimary else StkColors.Border, RoundedCornerShape(StkDimens.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = StkDimens.SpaceBase),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (selected) StkColors.Surface else StkColors.TextSecondary)
    }
}

@Composable
fun StkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    controlHeight: androidx.compose.ui.unit.Dp = StkDimens.PrimaryControlHeight,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        error != null -> StkColors.Error
        focused -> StkColors.BrandPrimary
        else -> StkColors.Border
    }
    val fieldHeight = if (singleLine) {
        Modifier.height(controlHeight)
    } else {
        Modifier.heightIn(min = StkDimens.MultilineControlMinHeight)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(fieldHeight)
                .onFocusChanged { focused = it.isFocused },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = StkColors.TextPrimary),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(StkColors.BrandPrimary),
            enabled = enabled,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = if (enabled) StkColors.Surface else StkColors.Background,
                            shape = RoundedCornerShape(StkDimens.RadiusControl),
                        )
                        .border(
                            width = StkDimens.Divider,
                            color = borderColor,
                            shape = RoundedCornerShape(StkDimens.RadiusControl),
                        )
                        .padding(horizontal = StkDimens.SpaceMd),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    leadingContent?.let {
                        it()
                        Spacer(Modifier.width(StkDimens.SpaceMd))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (singleLine) Modifier else Modifier.padding(vertical = StkDimens.SpaceMd)),
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                color = StkColors.TextTertiary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                    trailingContent?.invoke()
                }
            },
        )
        if (error != null) {
            Row(
                modifier = Modifier.padding(top = StkDimens.SpaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs),
            ) {
                StkErrorGlyph(modifier = Modifier.size(StkDimens.SpaceBase))
                Text(text = error, color = StkColors.Error, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StkTopBar(
    title: String,
    navigation: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        modifier = Modifier.height(StkDimens.TopBarHeight),
        navigationIcon = { navigation?.invoke() },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = StkColors.Surface,
            titleContentColor = StkColors.TextPrimary,
        ),
    )
}

@Composable
fun StkInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusCard))
            .border(
                width = StkDimens.Divider,
                color = StkColors.Border,
                shape = RoundedCornerShape(StkDimens.RadiusCard),
            )
            .padding(StkDimens.SpaceBase),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
fun StkSquareImagePlaceholder(
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = StkColors.BrandPrimary,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusControl))
            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}

val StkTransparent: Color = Color.Transparent
