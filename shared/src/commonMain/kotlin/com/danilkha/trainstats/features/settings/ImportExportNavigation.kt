package com.danilkha.trainstats.features.settings

import androidx.navigation.NavGraphBuilder

expect fun NavGraphBuilder.importExportScreens(onBack: () -> Unit)

expect val availableSettingsOptions: List<SettingsOption>
