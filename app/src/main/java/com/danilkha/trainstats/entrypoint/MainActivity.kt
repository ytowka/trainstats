package com.danilkha.trainstats.entrypoint

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import com.danilkha.trainstats.features.navigation.RootScreen
import com.danilkha.uikit.bottomsheet.LocalFragmentManager
import com.danilkha.uikit.theme.TrainingStatsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.BLACK

        setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalFragmentManager provides supportFragmentManager) {
                    RootScreen()
                }
            }
        }
    }
}
