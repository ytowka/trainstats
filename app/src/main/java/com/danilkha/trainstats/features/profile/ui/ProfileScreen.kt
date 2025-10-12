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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.uikit.components.Icon
import com.danilkha.uikit.components.TextToolbar
import com.danilkha.uikit.theme.Colors

@Composable
fun ProfileScreen(
    onSettingsClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextToolbar(
            title = stringResource(R.string.navigation_item_profile),
            endContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    onClick = onSettingsClicked
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