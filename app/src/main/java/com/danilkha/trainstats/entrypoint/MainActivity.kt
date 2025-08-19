package com.danilkha.trainstats.entrypoint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.danilkha.trainstats.features.navigation.RootScreen
import com.danilkha.uikit.bottomsheet.LocalFragmentManager
import com.danilkha.uikit.theme.TrainingStatsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isOptimizingIgnored = pm.isIgnoringBatteryOptimizations(packageName,)

        Log.d("debugg", "onCreate() isOptimizingIgnored = $isOptimizingIgnored ${packageName}")


        ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        Settings.ACTION_BATTERY_SAVER_SETTINGS
        val intent = Intent(
            ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        ).apply {
            //data = ("package:$packageName").toUri()
        }
        //val intent = Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")

        startActivity(intent)

        setContent {
            TrainingStatsTheme {
                CompositionLocalProvider(LocalFragmentManager provides supportFragmentManager) {
                    RootScreen()
                }
            }
        }
    }
}
