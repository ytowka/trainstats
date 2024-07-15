package com.danilkha.uikit.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ThemeTypography {
    val title = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    val subtitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    val body1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    )

    val body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )

    val button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}