package com.danilkha.uikit.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val lightColors = lightColors(
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
)

private val darkColors = darkColors(
    primary = Color(0xFF607D8B),
    primaryVariant = Color(0xFF7A9EAF),
    secondary = Color(0xFFDF4616),
    secondaryVariant = Color(0xFFE05B31),
    background = Color(0xFF0C0C0C),
    surface = Color(0xFF000000),
    error = Color(0xFFBE0B0B),
    onPrimary =Color(0xFF000000),
    onSecondary = Color(0xFF212121),
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6),
    onError = Color(0xFF000000),
)

val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

@Composable
fun TrainingStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if(darkTheme) darkColors else lightColors
    val designColors = if(darkTheme) darkDesignColors else lightDesignColors

    val typography = defaultThemeTextStyles.run { copy(
        title = title.copy(color = designColors.text),
        subtitle = subtitle.copy(color = designColors.text),
        body1 = body1.copy(color = designColors.text),
        body2 = body2.copy(color = designColors.text),
        button = button.copy(color = designColors.text),
    ) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }


    MaterialTheme(
        colors = colorScheme,
        shapes = shapes,
        content = {
            CompositionLocalProvider(
                LocalDesignColors provides designColors,
                LocalTextStyle provides ThemeTypography.body1.copy(color = designColors.text),
                LocalThemeTypography provides typography
            ) {
                content()
            }
        }
    )
}