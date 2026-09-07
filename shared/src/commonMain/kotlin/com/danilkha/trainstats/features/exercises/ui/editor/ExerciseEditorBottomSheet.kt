package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.bottomsheet.BottomSheetScreen
import com.danilkha.commonds.bottomsheet.BottomSheetState
import com.danilkha.commonds.bottomsheet.initOnArgs
import com.danilkha.commonds.bottomsheet.rememberBottomSheetState
import com.danilkha.commoncore.viewmodel.collectSingleEvents
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.theme.Colors
import com.danilkha.trainstats.features.exercises.ui.ExerciseListEvent
import com.danilkha.commonds.theme.PreviewContent
import com.danilkha.commonds.theme.ThemeTypography
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.trainstats.features.confirmdialog.AlertBottomSheetDialog
import com.danilkha.trainstats.features.confirmdialog.AlertDialogArgs
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

object ExerciseEditorBottomSheetArgs {
    internal val editingIdArg = "editing_id_arg"

    internal val result = "result"

    fun buildArgs(editingId: String): Map<String, Any> {
        return mapOf(
            editingIdArg to editingId
        )
    }
}

@Composable
fun ExerciseEditorBottomSheet(
    sheetState: BottomSheetState,
) {
    val viewModel = koinViewModel<ExerciseEditorViewModel>()

    sheetState.initOnArgs {
        val id = it[ExerciseEditorBottomSheetArgs.editingIdArg] as? String
        viewModel.processEvent(ExerciseEditorEvent.Init(id))
    }

    val state by viewModel.state.collectAsState()

    viewModel.collectSingleEvents {
        when (it) {
            ExerciseEditorSingleEvent.Saved -> {
                sheetState.setResult(mapOf(ExerciseEditorBottomSheetArgs.result to ExerciseListEvent.UpdateList))
                sheetState.hide()
            }
        }
    }

    val deleteAlertDialogBottomSheet = rememberBottomSheetState(
        onResult = {
            val button = it?.get(AlertDialogArgs.RESULT_BUTTON_ID)
            when (button) {
                AlertDialogArgs.CONFIRM_ID -> {
                    viewModel.processEvent(ExerciseEditorEvent.Delete)
                }

                AlertDialogArgs.DISMISS_ID -> {}
                AlertDialogArgs.CANCEL_ID -> {}
            }
        }
    )

    Box {
        BottomSheetScreen(
            state = sheetState
        ) {
            ExerciseEditorBottomSheet(
                state = state,
                onNameChange = { viewModel.processEvent(ExerciseEditorEvent.EditName(it)) },
                onSplitChange = { viewModel.processEvent(ExerciseEditorEvent.SetSeparated(it)) },
                onWeightChange = { viewModel.processEvent(ExerciseEditorEvent.SetWithWeight(it)) },
                onSaveClick = { viewModel.processEvent(ExerciseEditorEvent.Save) },
                onDeleteClick = {
                    deleteAlertDialogBottomSheet.show()
                },
                onCloseClicked = { sheetState.hide() },
            )
        }
        AlertBottomSheetDialog(
            sheetState = deleteAlertDialogBottomSheet,
            title = stringResource(Res.string.delete_exercise_title),
            text = stringResource(Res.string.delete_exercise_subtitle)
        )
    }
}

@Composable
private fun ExerciseEditorBottomSheet(
    state: ExerciseEditorState,
    onNameChange: (String) -> Unit,
    onSplitChange: (Boolean) -> Unit,
    onWeightChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClicked: () -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides ThemeTypography.body1
    ) {
        BottomSheetContent(
            modifier = Modifier.padding(10.dp),
            title = stringResource(when (state.mode) {
                    is ExerciseEditorMode.Edit -> Res.string.edit_exercise
                    ExerciseEditorMode.New -> Res.string.new_exercise
                }
            ),
            onCloseClicked = onCloseClicked
        ) {
            GenericTextFiled(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                onValueChange = onNameChange,
                hint = stringResource(Res.string.name)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TumblerRow(
                enabled = state.separated,
                text = stringResource(Res.string.separated),
                onValueChanged = onSplitChange
            )
            Spacer(modifier = Modifier.height(10.dp))
            TumblerRow(
                enabled = state.withWeight,
                text = stringResource(Res.string.has_weight),
                onValueChanged = onWeightChange
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GenericButton(
                    onClick = onSaveClick,
                    color = Colors.primary
                ) {
                    Text(
                        text = stringResource(Res.string.save),
                    )
                }
                if (state.mode is ExerciseEditorMode.Edit) {
                    GenericButton(
                        onClick = onDeleteClick,
                        color = Colors.error
                    ) {
                        Text(
                            text = stringResource(Res.string.delete),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}

@Composable
@Preview
fun ExerciseEditorBottomSheetPreview() {
    PreviewContent {
        ExerciseEditorBottomSheet(
            state = ExerciseEditorState(),
            onNameChange = {},
            onSplitChange = {},
            onWeightChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            onCloseClicked = {},
        )
    }
}

@Composable
fun TumblerRow(
    enabled: Boolean,
    text: String,
    onValueChanged: (Boolean) -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = enabled,
                onCheckedChange = onValueChanged
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
            )
        }
    }
}
