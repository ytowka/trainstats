package com.danilkha.navigation.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

interface Navigator {

    fun navigate(key: NavKey)

    /** @return true, если был осуществлён переход назад; false, если бэкстек пуст (мы в корне). */
    fun back(): Boolean
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator is not provided")
}
