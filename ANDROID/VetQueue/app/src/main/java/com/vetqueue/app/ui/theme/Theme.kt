package com.vetqueue.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val VetQueueColorScheme = lightColorScheme(
    primary = VetPurple,
    onPrimary = Color.White,
    primaryContainer = VetPurpleLight,
    onPrimaryContainer = VetPurpleDark,
    secondary = VetOrange,
    background = VetNeutralBg,
    surface = Color.White,
    error = VetRed,
    onBackground = VetTextPrimary,
    onSurface = VetTextPrimary,
)

private val VetQueueTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 12.sp),
)

@Composable
fun VetQueueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VetQueueColorScheme,
        typography = VetQueueTypography,
        content = content
    )
}
