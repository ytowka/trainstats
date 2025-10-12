package com.danilkha.trainstats.features.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danilkha.trainstats.R
import com.danilkha.trainstats.features.settings.export.ui.ExportScreenPage
import com.danilkha.trainstats.features.settings.workoutimport.ui.ImportScreen
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.TextToolbar
import com.danilkha.uikit.theme.ThemeTypography

object SettingsDestinations {
    const val SETTINGS = "Settings"
}

enum class SettingsOption(@StringRes val titleRes: Int) {
    Import(R.string.to_import),
    Export(R.string.to_export)
}

@Composable
fun SettingsHostScreen(
    onBack: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "settings"
    ) {
        composable(SettingsDestinations.SETTINGS) {
            SettingsScreen(
                onBack = onBack,
                onItemClicked = {
                    navController.navigate(it.name)
                }
            )
        }
        composable(SettingsOption.Import.name) {
            ImportScreen(
                onBack = { navController.navigateUp() }
            )
        }
        composable(SettingsOption.Export.name) {
            ExportScreenPage(
                onBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onItemClicked: (SettingsOption) -> Unit,
) {
    Column {
        TextToolbar(
            title = stringResource(id = R.string.settings),
            onBack = onBack
        )
        Card(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .padding(horizontal = 10.dp)
                .weight(1f),
            contentPadding = PaddingValues()
        ) {
            SettingsOption.entries.forEach {
                SettingsItem(
                    title = stringResource(it.titleRes),
                    onClick = { onItemClicked(it) }
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