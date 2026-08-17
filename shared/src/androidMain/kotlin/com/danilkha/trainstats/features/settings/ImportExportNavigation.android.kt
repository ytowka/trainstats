package com.danilkha.trainstats.features.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.danilkha.trainstats.features.settings.export.ui.ExportScreenPage
import com.danilkha.trainstats.features.settings.workoutimport.ui.ImportScreenRoute

actual fun NavGraphBuilder.importExportScreens(onBack: () -> Unit) {
    composable(SettingsOption.Import.name) {
        ImportScreenRoute(onBack = onBack)
    }
    composable(SettingsOption.Export.name) {
        ExportScreenPage(onBack = onBack)
    }
}

actual val availableSettingsOptions: List<SettingsOption> =
    listOf(SettingsOption.Import, SettingsOption.Export)
