package com.danilkha.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


internal val defaultThemeTextStyles = ThemeTextStyles(
    title = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    subtitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),

    body1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),

    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
)

data class ThemeTextStyles(
    val title: TextStyle,
    val subtitle: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
)

val LocalThemeTypography = staticCompositionLocalOf { defaultThemeTextStyles }

val ThemeTypography
    @Composable @ReadOnlyComposable get() = LocalThemeTypography.current