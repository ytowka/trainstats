package com.danilkha.uikit.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class DesignColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val text: Color,
    val isLight: Boolean
)

val lightDesignColors = DesignColors(
    primary = Color(0xFF607D8B),
    primaryVariant = Color(0xFF455A64),
    secondary = Color(0xFFFF5722),
    secondaryVariant = Color(0xFFCE4317),
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFBE0B0B),
    onPrimary =Color(0xFFFFFFFF) ,
    onSecondary = Color(0xFFBDBDBD),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    onError = Color(0xFFFFFFFF),
    text = Color(0xFF000000),
    isLight = true
)

val darkDesignColors = DesignColors(
    primary = Color(0xFF607D8B),
    primaryVariant = Color(0xFF7A9EAF),
    secondary = Color(0xFFDF4616),
    secondaryVariant = Color(0xFFE05B31),
    background = Color(0xFF0C0C0C),
    surface = Color(0xFF000000),
    error = Color(0xFFBE0B0B),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF212121),
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6),
    onError = Color(0xFF000000),
    text = Color(0xFFFFFFFF),
    isLight = false
)

val LocalDesignColors = staticCompositionLocalOf { lightDesignColors }

val Colors: DesignColors
    @Composable @ReadOnlyComposable get() = LocalDesignColors.current
