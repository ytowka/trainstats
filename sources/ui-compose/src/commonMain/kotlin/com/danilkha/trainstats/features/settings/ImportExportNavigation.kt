package com.danilkha.trainstats.features.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** Регистрирует платформо-зависимые экраны настроек (Import/Export) в общем бэкстеке. */
expect fun EntryProviderScope<NavKey>.importExportEntries()

val availableSettingsOptions = listOf(SettingsOption.Import, SettingsOption.Export)
