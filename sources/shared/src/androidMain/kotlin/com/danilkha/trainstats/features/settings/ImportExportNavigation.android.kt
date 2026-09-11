package com.danilkha.trainstats.features.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.danilkha.navigation.api.destinations.ExportNav
import com.danilkha.navigation.api.destinations.ImportNav
import com.danilkha.trainstats.features.settings.export.ui.ExportScreenPage
import com.danilkha.trainstats.features.settings.workoutimport.ui.ImportScreenRoute

actual fun EntryProviderScope<NavKey>.importExportEntries() {
    entry<ImportNav> {
        ImportScreenRoute()
    }
    entry<ExportNav> {
        ExportScreenPage()
    }
}

actual val availableSettingsOptions: List<SettingsOption> =
    listOf(SettingsOption.Import, SettingsOption.Export)
