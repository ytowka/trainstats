package com.danilkha.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Paddings(
    val tiny: Dp = 4.dp,
    val small: Dp = 8.dp,
    val reduced: Dp = 12.dp,
    val normal: Dp = 16.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

val LocalPaddings = staticCompositionLocalOf { Paddings() }

