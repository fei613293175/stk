package com.zzyihao.stk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StkLightColors = lightColorScheme(
    primary = StkColors.BrandPrimary,
    onPrimary = StkColors.Surface,
    primaryContainer = StkColors.BrandPrimarySoft,
    onPrimaryContainer = StkColors.TextPrimary,
    secondary = StkColors.BrandAccent,
    onSecondary = StkColors.Surface,
    secondaryContainer = StkColors.BrandAccentSoft,
    onSecondaryContainer = StkColors.TextPrimary,
    background = StkColors.Background,
    onBackground = StkColors.TextPrimary,
    surface = StkColors.Surface,
    onSurface = StkColors.TextPrimary,
    outline = StkColors.Border,
    error = StkColors.Error,
)

@Composable
fun StkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StkLightColors,
        typography = StkTypography,
        content = content,
    )
}
