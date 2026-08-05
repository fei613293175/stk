package com.zzyihao.stk.designsystem

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object StkTokens {
    val BrandPrimary = Color(0xFF246BFD)
    val BrandAccent = Color(0xFFFF7A1A)
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF172033)
    val TextSecondary = Color(0xFF667085)
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
}

@Composable
fun StkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = StkTokens.BrandPrimary, secondary = StkTokens.BrandAccent, background = StkTokens.Background, surface = StkTokens.Surface, onSurface = StkTokens.TextPrimary), content = content)
}
