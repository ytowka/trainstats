package com.danilkha.trainstats.features.exercises.ui.editor

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.collectSingleEvents
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.core.viewmodel.viewModel
import com.danilkha.uikit.bottomsheet.ComposeContextBottomDialog
import com.danilkha.uikit.components.BottomSheetContent
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.GenericTextFiled
import com.danilkha.uikit.theme.Colors
import com.danilkha.trainstats.features.confirmdialog.rememberAlertDialog

class ExerciseEditorBottomSheet : ComposeContextBottomDialog(){


    private val viewModel by viewModel { it.exerciseEditorViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments?.containsKey(editingIdArg) == true){
            val id = arguments?.getLong(editingIdArg, 0)
            viewModel.init(id)
            arguments?.remove(editingIdArg)
        }
    }

    override val content: @Composable () -> Unit = {

        val state by viewModel.state.collectAsState()

        val listViewModel = getCurrentViewModel { it.exerciseListViewModel }

        viewModel.collectSingleEvents {
            when(it){
                ExerciseEditorSingleEvent.Saved -> {
                    listViewModel.updateList()
                    dismiss()
                }
            }
        }

        val deleteAlertDialogFragment = rememberAlertDialog(
            titleRes = R.string.delete_exercise_title,
            textRes = R.string.delete_exercise_subtitle,
            dialogKey = "delete",
            onConfirm = {
                viewModel.delete()
            },
            onCancel = { })

        ExerciseEditorBottomSheet(
            state = state,
            onNameChange = viewModel::editName,
            onSplitChange = viewModel::setSeparated,
            onWeightChange = viewModel::setWithWeight,
            onSaveClick = viewModel::save,
            onDeleteClick = {
                deleteAlertDialogFragment.show()
            },
            onCloseClicked = { dismiss() },
        )
    }

    companion object{
        private val editingIdArg = "editing_id_arg"

        fun buildArgs(editingId: Long): Bundle{
            return bundleOf(
                editingIdArg to editingId
            )
        }
    }
}

@Composable
fun ExerciseEditorBottomSheet(
    state: ExerciseEditorState,
    onNameChange: (String) -> Unit,
    onSplitChange: (Boolean) -> Unit,
    onWeightChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClicked: () -> Unit,
){
    BottomSheetContent(
        title = stringResource(id = when(state.mode){
            is ExerciseEditorMode.Edit -> R.string.edit_exercise
            ExerciseEditorMode.New -> R.string.new_exercise
        }),
        onCloseClicked = onCloseClicked
    ) {
        GenericTextFiled(
            modifier = Modifier.fillMaxWidth(),
            value = state.name,
            onValueChange = onNameChange,
            hint = stringResource(id = R.string.name)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TumblerRow(
            enabled = state.separated,
            text = stringResource(id = R.string.separated),
            onValueChanged = onSplitChange
        )
        Spacer(modifier = Modifier.height(10.dp))
        TumblerRow(
            enabled = state.withWeight,
            text = stringResource(id = R.string.has_weight),
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
                    text = stringResource(id = R.string.save),
                )
            }
            if(state.mode is ExerciseEditorMode.Edit){
                GenericButton(
                    onClick = onDeleteClick,
                    color = Colors.error
                ) {
                    Text(
                        text = stringResource(id = R.string.delete),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TumblerRow(
    enabled: Boolean,
    text: String,
    onValueChanged: (Boolean) -> Unit
){
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