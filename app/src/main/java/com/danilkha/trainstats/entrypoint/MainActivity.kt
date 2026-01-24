package com.danilkha.trainstats.entrypoint

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.FragmentActivity
import com.danilkha.trainstats.core.utils.JvmDateTimeFormatter
import com.danilkha.trainstats.core.utils.LocalDateFormat
import com.danilkha.trainstats.features.navigation.RootScreen
import com.danilkha.uikit.bottomsheet.LocalFragmentManager
import com.danilkha.commonds.theme.TrainingStatsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.BLACK
        val dateFormat = JvmDateTimeFormatter(this)

        setContent {
            TrainingStatsTheme {
                setStatusBarAppearance(!isSystemInDarkTheme())
                CompositionLocalProvider(
                    LocalFragmentManager provides supportFragmentManager,
                    LocalDateFormat provides dateFormat
                ) {
                    RootScreen()
                }
            }
        }
    }
}

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
