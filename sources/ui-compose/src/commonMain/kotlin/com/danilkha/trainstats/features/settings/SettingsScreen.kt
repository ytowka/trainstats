package com.danilkha.trainstats.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.navigation.api.LocalNavigator
import com.danilkha.navigation.api.destinations.ExportNav
import com.danilkha.navigation.api.destinations.ImportNav
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.settings
import training_stats.shared.generated.resources.to_export
import training_stats.shared.generated.resources.to_import

enum class SettingsOption(val titleRes: StringResource, val destination: NavKey) {
    Import(Res.string.to_import, ImportNav),
    Export(Res.string.to_export, ExportNav),
}

@Composable
fun SettingsScreen() {
    val navigator = LocalNavigator.current

    Column {
        TextToolbar(
            title = stringResource(Res.string.settings),
            onBack = { navigator.back() }
        )
        Card(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .padding(horizontal = 10.dp)
                .weight(1f),
            contentPadding = PaddingValues()
        ) {
            availableSettingsOptions.forEach {
                SettingsItem(
                    title = stringResource(it.titleRes),
                    onClick = { navigator.navigate(it.destination) }
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit
){
    Box {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 20.dp, horizontal = 32.dp)
            ,
            text = title,
        )
        Divider(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
