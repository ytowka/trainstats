package com.danilkha.trainstats.entrypoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import com.danilkha.trainstats.features.navigation.RootScreen
import com.danilkha.uikit.bottomsheet.LocalFragmentManager
import com.danilkha.uikit.theme.TrainingStatsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalFragmentManager provides supportFragmentManager) {
                    RootScreen()
                }
            }
        }
    }
}
