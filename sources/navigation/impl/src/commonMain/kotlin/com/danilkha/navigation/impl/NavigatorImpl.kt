package com.danilkha.navigation.impl

import androidx.navigation3.runtime.NavKey
import com.danilkha.navigation.api.Navigator

internal class NavigatorImpl(
    private val backStack: MutableList<NavKey>,
) : Navigator {

    override fun navigate(key: NavKey) {
        backStack.add(key)
    }

    override fun back(): Boolean = backStack.removeLastOrNull() != null
}
