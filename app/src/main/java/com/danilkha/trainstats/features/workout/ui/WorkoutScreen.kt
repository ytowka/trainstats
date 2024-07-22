package com.danilkha.trainstats.features.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.format
import com.danilkha.trainstats.core.viewmodel.LaunchCollectEffects
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.confirmdialog.rememberAlertDialog
import com.danilkha.trainstats.features.exercises.ui.ExerciseListSideEffect
import com.danilkha.trainstats.features.exercises.ui.selector.ExerciseSelectorBottomSheet
import com.danilkha.trainstats.features.workout.ui.components.ExerciseGroupCard
import com.danilkha.uikit.bottomsheet.rememberBottomSheetController
import com.danilkha.uikit.components.DateSelector
import com.danilkha.uikit.components.DragAndDropColumn
import com.danilkha.uikit.components.DragDispatcher
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.Icon
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography

@Composable
fun WorkoutScreenRoute(
    viewModel: WorkoutViewModel = viewModel()
){
    val state by viewModel.state.collectAsState(viewModel.startState)

    var showDateDialog by rememberSaveable { mutableStateOf(false) }

    val alertDialog = rememberAlertDialog(
        titleRes = R.string.delete_workout_title,
        textRes = R.string.delete_exercise_subtitle,
        dialogKey = "delete",
        onConfirm = viewModel::deleteWorkout,
        onCancel = {  })

    val exerciseSelectorViewModel = getCurrentViewModel { it.exerciseListViewModel }
    val exerciseSelector = rememberBottomSheetController(
        bottomSheetClass = ExerciseSelectorBottomSheet::class.java
    )
    exerciseSelectorViewModel.LaunchCollectEffects{
        when(it){
            is ExerciseListSideEffect.ExerciseClicked -> {
                viewModel.addExercise(it.exerciseModel)
                exerciseSelector.hide()
            }
        }
    }


    if(showDateDialog){
        DateSelector(
            onDismiss = {showDateDialog = false},
            onDateSelected = {
                viewModel.changeDate(it)
                showDateDialog = false
            }
        )
    }

    WorkoutScreen(
        state = state,
        onDateClicked = { showDateDialog = true },
        onWeightChange = viewModel::editWeight,
        onRepsChange = viewModel::editReps,
        onDeleteSet = viewModel::deleteSet,
        onReturnDeleted = viewModel::returnDeletedSet,
        onSetMoved = viewModel::onSetMove,
        onGroupMoved = viewModel::onGroupMove,
        onExpandClick = viewModel::toggleGroup,
        onSave = viewModel::saveWorkout,
        addExercise = {
            exerciseSelector.show()
        },
        onDelete = alertDialog::show,
        onDeleteGroup = viewModel::deleteGroup
    )
}

@Composable
fun WorkoutScreen(
    state: WorkoutState,

    onDateClicked: () -> Unit,
    onWeightChange: (groupIndex: Int,index: Int, Float) -> Unit,
    onRepsChange: (groupIndex: Int,index: Int, Side?, value: Float) -> Unit,
    onDeleteSet: (groupIndex: Int,index: Int) -> Unit,
    onReturnDeleted: (groupIndex: Int,index: Int) -> Unit,
    onSetMoved: (groupIndex: Int, from: Int, to: Int) -> Unit,
    onGroupMoved: (from: Int, to: Int) -> Unit,
    onExpandClick: (groupIndex: Int,) -> Unit,
    onDeleteGroup: (Int) -> Unit,

    addExercise: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = Colors.background)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onDateClicked)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            text = state.date.format(),
            style = ThemeTypography.title,
            color = Colors.primary
        )

        val dragDispatcher = remember { DragDispatcher() }

        DragAndDropColumn(
            items = state.groups,
            onItemMoved = onGroupMoved,
            dragDispatcher = dragDispatcher,
            keyProvider = { index, it -> it.groupTempId }
        ) { groupIndex, item ->
            val groupIndexUpdated by rememberUpdatedState(newValue = groupIndex)
           Box(modifier = Modifier.padding(vertical = 5.dp)) {
               ExerciseGroupCard(
                   title = item.name,
                   expanded = item.groupTempId !in state.collapsedGroupIds,
                   sets = item.sets,
                   deleted = state.pendingDelete,
                   onWeightChange = { index, value -> onWeightChange(groupIndex, index, value) },
                   onRepsChange =  { index, side, value -> onRepsChange(groupIndex, index, side, value) },
                   onDelete = { onDeleteSet(groupIndex, it) },
                   onReturnDeleted = { onReturnDeleted(groupIndex, it) },
                   onSetMoved = { from, to ->  onSetMoved(groupIndex, from, to) },
                   onExpandClick = { onExpandClick(groupIndex) },
                   separated = item.separated,
                   hasWeight = item.hasWeight,

                   onDragStart = { dragDispatcher.onDragStart(groupIndexUpdated) },
                   onDragEnd = dragDispatcher::onDragEnd,
                   onVerticalDrag = dragDispatcher::onDrag,
                   onDeleteGroup = { onDeleteGroup(groupIndex) }
               )
           }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(50))
                .clickable(onClick = addExercise)
                .padding(vertical = 5.dp, horizontal = 10.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.add_exercise),
                color = Colors.secondary,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Default.Add,
                color = Colors.secondary,
            )
        }
        Spacer(modifier = Modifier.size(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isSaved = state.initialWorkout != null && state.initialWorkout.saved
            if (isSaved){
                GenericButton(
                    onClick = onSave,
                    color = Colors.error
                ) {
                    Text(
                        stringResource(id = R.string.delete),
                        color = Colors.textInverse
                    )
                }
            }
            GenericButton(
                onClick = onSave,
            ) {
                Text(text = if(isSaved) stringResource(id = R.string.update)
                else stringResource(id = R.string.save))
            }
        }
    }
}

