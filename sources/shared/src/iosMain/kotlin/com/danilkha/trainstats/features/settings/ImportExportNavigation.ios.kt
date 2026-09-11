package com.danilkha.trainstats.features.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

actual fun EntryProviderScope<NavKey>.importExportEntries() {
    // iOS does not support export/import in this iteration (Phase 3 decision).
}

actual val availableSettingsOptions: List<SettingsOption> = emptyList()
