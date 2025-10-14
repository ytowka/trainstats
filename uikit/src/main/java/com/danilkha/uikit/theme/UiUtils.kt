package com.danilkha.uikit.theme

import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

@ReadOnlyComposable
@Composable
fun setStatusBarAppearance(isLightStatusBar: Boolean){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val style = if(isLightStatusBar) {
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        } else {
            0
        }
        LocalView.current.windowInsetsController?.setSystemBarsAppearance(
            style,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        val style = if (isLightStatusBar) {
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            0
        }
        LocalActivity.current?.window?.decorView?.let { decorView ->
            decorView.systemUiVisibility = style
        }
    }
}

@Composable fun Modifier.insetPaddings(): Modifier {
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val bottomPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    return this.padding(
        top = topPadding,
        bottom = bottomPadding,
    )
}

@Composable
fun PreviewContent(content: @Composable BoxScope.() -> Unit) {
    TrainingStatsTheme {
        Box(
            modifier = Modifier.background(color = MaterialTheme.colors.background),
            content = content
        )
    }
}