package com.danilkha.trainstats.features.settings.export.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.navigation.api.LocalNavigator
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun ExportScreenPage(
    viewModel: ExportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.current

    ExportPage(
        state = state,
        onExportClicked = { viewModel.processEvent(ExportEvent.Export) },
        onBack = { navigator.back() }
    )
}

@Composable
fun ExportPage(
    state: ExportState,
    onExportClicked: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextToolbar(
            title = stringResource(Res.string.export_workout),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .height(200.dp)
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(state) {
                ExportState.Init -> {
                    GenericButton(onClick = onExportClicked) {
                        Text(stringResource(Res.string.to_export))
                    }
                }
                ExportState.Loading -> {
                    CircularProgressIndicator()
                }
                is ExportState.Saved -> {
                    Icon(imageVector = Icons.Default.Done)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = stringResource(Res.string.success_export, state.filename),
                        textAlign = TextAlign.Center
                    )
                }
                is ExportState.Error -> {
                    Icon(imageVector = Icons.Default.Close)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = state.throwable.message.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
