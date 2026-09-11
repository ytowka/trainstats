package com.danilkha.navigation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.danilkha.navigation.api.LocalNavigator
import com.danilkha.navigation.api.destinations.ExportNav
import com.danilkha.navigation.api.destinations.ImportNav
import com.danilkha.navigation.api.destinations.RootNav
import com.danilkha.navigation.api.destinations.SettingsNav
import com.danilkha.navigation.api.destinations.WorkoutNav
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Хост навигации приложения. Владеет бэкстеком, предоставляет [com.danilkha.navigation.api.Navigator]
 * через [com.danilkha.navigation.api.LocalNavigator] и рендерит текущий entry через [NavDisplay].
 */
@Composable
fun NavHost(
    startDestination: NavKey = RootNav,
    modifier: Modifier = Modifier,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    val backStack = rememberNavBackStack(
        NavKeySavedStateConfiguration,
        startDestination,
    )
    val navigator = remember(backStack) { NavigatorImpl(backStack) }

    // На Android owner — Activity; на iOS в корне композиции owner нет, создаём свой.
    val parentOwner = LocalViewModelStoreOwner.current
        ?: remember { RootViewModelStoreOwner() }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalViewModelStoreOwner provides parentOwner,
    ) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = { navigator.back() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider,
        )
    }
}

private class RootViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

private val NavKeySavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(RootNav::class)
            subclass(SettingsNav::class)
            subclass(WorkoutNav::class)
            subclass(ImportNav::class)
            subclass(ExportNav::class)
        }
    }
}
