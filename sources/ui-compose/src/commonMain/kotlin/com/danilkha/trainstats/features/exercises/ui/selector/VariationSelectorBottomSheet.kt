package com.danilkha.trainstats.features.exercises.ui.selector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.danilkha.commonds.components.Card
import com.danilkha.trainstats.features.exercises.ui.components.VariationOptionCard
import androidx.compose.ui.unit.dp
import com.danilkha.commoncore.viewmodel.LaunchCollectEffects
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.theme.Colors
import com.danilkha.trainstats.bottomsheet.BottomSheetScreen
import com.danilkha.trainstats.bottomsheet.BottomSheetState
import com.danilkha.trainstats.bottomsheet.initOnArgs
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import training_stats.shared.generated.resources.*

@Composable
fun VariationSelectorBottomSheet(sheetState: BottomSheetState, onSelected: (Long, ExerciseVariation?) -> Unit) {
    val viewModel = koinViewModel<VariationSelectorViewModel>()
    val stateFlow = remember(viewModel) { viewModel.state }
    val state by stateFlow.collectAsState()
    var groupId by remember { mutableStateOf(0L) }
    sheetState.initOnArgs {
        groupId = it["groupId"] as Long
        viewModel.processEvent(VariationSelectorEvent.Init(it["exerciseId"] as String, it["variationId"] as? String))
    }
    viewModel.LaunchCollectEffects {
        onSelected(groupId, it.variation)
        sheetState.hide()
    }
    BottomSheetScreen(sheetState) {
        BottomSheetContent(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(10.dp),
            title = stringResource(Res.string.variations), onCloseClicked = { sheetState.hide() },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VariationOptionCard(
                    name = stringResource(Res.string.ordinary_variation),
                    selected = state.selectedVariationId == null,
                    modifier = Modifier.testTag("variation_option_ordinary"),
                    onClick = { viewModel.processEvent(VariationSelectorEvent.Select(null)) },
                )
                state.variations.forEach { variation ->
                    VariationOptionCard(
                        name = variation.name,
                        selected = state.selectedVariationId == variation.id,
                        modifier = Modifier.testTag("variation_option_${variation.id}"),
                        onClick = { viewModel.processEvent(VariationSelectorEvent.Select(variation)) },
                    )
                }
            }
            if (state.loading) CircularProgressIndicator(color = Colors.primary)
            if (state.name == null) {
                TextButton(onClick = { viewModel.processEvent(VariationSelectorEvent.Add) }) {
                    Text(stringResource(Res.string.add_variation), color = Colors.primary)
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenericTextFiled(value = state.name.orEmpty(), onValueChange = { viewModel.processEvent(VariationSelectorEvent.EditName(it)) },
                        modifier = Modifier.fillMaxWidth(), hint = stringResource(Res.string.variation_name))
                    if (state.duplicate) Text(stringResource(Res.string.variation_names_invalid), color = Colors.error)
                    Button(
                        enabled = state.canCreate, onClick = { viewModel.processEvent(VariationSelectorEvent.Create) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Colors.primary, contentColor = Colors.textInverse),
                    ) { Text(stringResource(Res.string.create_variation)) }
                }
            }
            if (state.failed) Text(stringResource(Res.string.variation_save_failed), color = Colors.error)
        }
    }
}
