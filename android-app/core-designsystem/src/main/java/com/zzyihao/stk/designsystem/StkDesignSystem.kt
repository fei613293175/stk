package com.zzyihao.stk.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object StkTokens {
    val BrandPrimary = Color(0xFF246BFD)
    val BrandAccent = Color(0xFFFF7A1A)
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF172033)
    val TextSecondary = Color(0xFF667085)
    val TextTertiary = Color(0xFF98A2B3)
    val Border = Color(0xFFE4E7EC)
    val Divider = Color(0xFFEAECF0)
    val Success = Color(0xFF16A34A)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFE5484D)
    val BrandPrimaryPressed = Color(0xFF1D56D8)
    val BrandPrimarySoft = Color(0xFFEAF1FF)
    val SurfaceSecondary = Color(0xFFF9FAFB)
    val Disabled = Color(0xFFD0D5DD)
    val OverlayScrim = Color(0x66000000)
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space6 = 6.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space14 = 14.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space40 = 40.dp
    val Radius4 = 4.dp
    val Radius6 = 6.dp
    val Radius8 = 8.dp
    val Radius12 = 12.dp
    val Radius16 = 16.dp
    val Radius20 = 20.dp
    val Radius24 = 24.dp
    val TopBarHeight = 56.dp
    val BottomNavHeight = 64.dp
    val ButtonHeight = 48.dp
    val SecondaryButtonHeight = 40.dp
    val InputHeight = 48.dp
    val TouchMin = 48.dp
    val IconDefault = 24.dp
    val IconSmall = 20.dp
    val AvatarProfile = 72.dp
    val ProjectThumbnail = 80.dp
    val CardPadding = 16.dp
    val ScreenPadding = 16.dp
    val ListGap = 12.dp
    val SectionGap = 20.dp
    val FieldGap = 12.dp
    val CaptchaImageWidth = 278.dp
    val CaptchaImageHeight = 96.dp
    val DialogWidth = 326.dp
    val AuthTopGap = 48.dp
    val AuthLogoSize = 64.dp

    val Display = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
    val AmountLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
    val TitleLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
    val PageTitle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
    val SectionTitle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val CardTitle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
    val Body = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    val BodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
    val Secondary = TextStyle(fontSize = 13.sp, lineHeight = 20.sp)
    val Caption = TextStyle(fontSize = 12.sp, lineHeight = 18.sp)
    val NavLabel = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun StkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = StkTokens.BrandPrimary,
            onPrimary = Color.White,
            primaryContainer = StkTokens.BrandPrimarySoft,
            onPrimaryContainer = StkTokens.TextPrimary,
            secondary = StkTokens.BrandAccent,
            background = StkTokens.Background,
            onBackground = StkTokens.TextPrimary,
            surface = StkTokens.Surface,
            onSurface = StkTokens.TextPrimary,
            surfaceVariant = StkTokens.SurfaceSecondary,
            outline = StkTokens.Border,
            error = StkTokens.Error,
        ),
        typography = Typography(
            displaySmall = StkTokens.Display,
            headlineSmall = StkTokens.PageTitle,
            titleLarge = StkTokens.TitleLarge,
            titleMedium = StkTokens.SectionTitle,
            bodyLarge = StkTokens.Body,
            bodyMedium = StkTokens.BodyMedium,
            bodySmall = StkTokens.Caption,
            labelLarge = StkTokens.CardTitle,
            labelMedium = StkTokens.NavLabel,
        ),
        shapes = Shapes(
            small = RoundedCornerShape(StkTokens.Radius8),
            medium = RoundedCornerShape(StkTokens.Radius12),
            large = RoundedCornerShape(StkTokens.Radius20),
        ),
        content = content,
    )
}

@Composable
fun StkRootScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StkTokens.Background,
        topBar = topBar,
        bottomBar = { StkBottomNavigation(selectedTab, onTabSelected) },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StkPageScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StkTokens.Background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = StkTokens.PageTitle) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack, modifier = Modifier.testTag("page_back"), contentPadding = PaddingValues()) { Text("‹", style = StkTokens.Display) }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StkTokens.Surface),
            )
        },
        content = content,
    )
}

@Composable
fun StkBottomNavigation(selected: Int, onSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = StkTokens.Surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(StkTokens.BottomNavHeight),
    ) {
        listOf("首页", "发布", "我的").forEachIndexed { index, label ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelected(index) },
                icon = { Text(if (index == 0) "⌂" else if (index == 1) "+" else "♙", style = StkTokens.TitleLarge) },
                label = { Text(label, style = StkTokens.NavLabel) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StkTokens.BrandPrimary,
                    selectedTextColor = StkTokens.BrandPrimary,
                    unselectedIconColor = StkTokens.TextTertiary,
                    unselectedTextColor = StkTokens.TextTertiary,
                    indicatorColor = StkTokens.BrandPrimarySoft,
                ),
                modifier = Modifier.testTag(listOf("nav_home", "nav_publish", "nav_me")[index]),
            )
        }
    }
}

@Composable
fun StkPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, tag: String? = null, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = StkTokens.ButtonHeight).then(if (tag == null) Modifier else Modifier.testTag(tag)),
        shape = RoundedCornerShape(StkTokens.Radius12),
        contentPadding = PaddingValues(horizontal = StkTokens.Space16),
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(StkTokens.IconSmall), color = Color.White, strokeWidth = 2.dp) else Text(text, style = StkTokens.CardTitle)
    }
}

@Composable
fun StkSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, tag: String? = null) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = StkTokens.SecondaryButtonHeight).then(if (tag == null) Modifier else Modifier.testTag(tag)),
        shape = RoundedCornerShape(StkTokens.Radius12),
        border = BorderStroke(1.dp, StkTokens.BrandPrimary),
        contentPadding = PaddingValues(horizontal = StkTokens.Space14),
    ) { Text(text, style = StkTokens.BodyMedium, color = StkTokens.BrandPrimary) }
}

@Composable
fun StkTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, tag: String? = null, error: String? = null, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        label = { Text(label, style = StkTokens.Body) },
        isError = !error.isNullOrBlank(),
        supportingText = { if (!error.isNullOrBlank()) Text(error, style = StkTokens.Caption, color = StkTokens.Error) },
        shape = RoundedCornerShape(StkTokens.Radius12),
        modifier = modifier.heightIn(min = StkTokens.InputHeight).then(if (tag == null) Modifier else Modifier.testTag(tag)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StkTokens.BrandPrimary,
            unfocusedBorderColor = StkTokens.Border,
            focusedLabelColor = StkTokens.BrandPrimary,
        ),
    )
}

@Composable
fun StkPasswordField(value: String, onValueChange: (String) -> Unit, label: String, visible: Boolean, onToggleVisible: () -> Unit, modifier: Modifier = Modifier, tag: String? = null, visibilityTag: String? = null, error: String? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label, style = StkTokens.Body) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = !error.isNullOrBlank(),
        supportingText = { if (!error.isNullOrBlank()) Text(error, style = StkTokens.Caption, color = StkTokens.Error) },
        trailingIcon = { TextButton(onClick = onToggleVisible, modifier = Modifier.testTag(visibilityTag ?: "password_visibility"), contentPadding = PaddingValues()) { Text(if (visible) "隐藏" else "显示", style = StkTokens.Caption) } },
        shape = RoundedCornerShape(StkTokens.Radius12),
        modifier = modifier.heightIn(min = StkTokens.InputHeight).then(if (tag == null) Modifier else Modifier.testTag(tag)),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StkTokens.BrandPrimary, unfocusedBorderColor = StkTokens.Border),
    )
}

@Composable
fun StkStatusMessage(title: String, message: String, tone: StkStatusTone = StkStatusTone.Info, actionLabel: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val color = when (tone) { StkStatusTone.Success -> StkTokens.Success; StkStatusTone.Warning -> StkTokens.Warning; StkStatusTone.Error -> StkTokens.Error; StkStatusTone.Info -> StkTokens.BrandPrimary }
    Column(modifier.fillMaxWidth().background(color.copy(alpha = 0.08f), RoundedCornerShape(StkTokens.Radius16)).padding(StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space8)) {
        Text(title, style = StkTokens.SectionTitle, color = color)
        Text(message, style = StkTokens.Body, color = StkTokens.TextSecondary)
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction, contentPadding = PaddingValues()) { Text(actionLabel, color = color) }
    }
}

enum class StkStatusTone { Info, Success, Warning, Error }

@Composable
fun StkBrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(StkTokens.AuthLogoSize)
            .background(StkTokens.BrandPrimary, RoundedCornerShape(StkTokens.Radius20)),
        contentAlignment = Alignment.Center,
    ) {
        Text("↗", style = StkTokens.Display, color = Color.White)
    }
}

@Composable
fun StkAuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = StkTokens.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StkTokens.Space12),
    ) {
        Spacer(Modifier.height(StkTokens.AuthTopGap))
        StkBrandMark()
        Text(title, style = StkTokens.Display, color = StkTokens.TextPrimary)
        Text(subtitle, style = StkTokens.TitleLarge, color = StkTokens.TextSecondary)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(StkTokens.FieldGap),
            content = content,
        )
    }
}
