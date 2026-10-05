package com.danilkha.trainstats.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.commonds.theme.Colors
import com.danilkha.navigation.api.LocalNavigator
import com.danilkha.navigation.api.destinations.SettingsNav
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun ProfileScreen() {
    val navigator = LocalNavigator.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextToolbar(
            title = stringResource(Res.string.navigation_item_profile),
            endContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    onClick = { navigator.navigate(SettingsNav) }
                )
            }
        )
        Spacer(modifier = Modifier.size(100.dp))
        Icon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(color = Colors.surface)
                .size(80.dp),
            imageVector = Icons.Default.Person
        )

    }
}
