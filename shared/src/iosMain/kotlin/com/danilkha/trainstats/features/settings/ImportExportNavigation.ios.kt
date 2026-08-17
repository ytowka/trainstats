package com.danilkha.trainstats.features.settings

import androidx.navigation.NavGraphBuilder

actual fun NavGraphBuilder.importExportScreens(onBack: () -> Unit) {
    // iOS does not support export/import in this iteration (Phase 3 decision).
}

actual val availableSettingsOptions: List<SettingsOption> = emptyList()
