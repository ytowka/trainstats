package com.danilkha.trainstats.features.workout.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commonds.bottomsheet.rememberBottomSheetState
import com.danilkha.trainstats.core.utils.LocalDateFormat
import com.danilkha.trainstats.core.viewmodel.LaunchCollectEffects
import com.danilkha.trainstats.features.exercises.ui.ExerciseListEvent
import com.danilkha.trainstats.features.exercises.ui.ExerciseListSideEffect
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.selector.ExerciseSelectorBottomSheet
import com.danilkha.trainstats.features.workout.ui.components.ExerciseGroupCard
import com.danilkha.commonds.components.DragAndDropColumn
import com.danilkha.commonds.components.DragDispatcher
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.datepicker.DateSelector
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheet
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheetArgs
import com.danilkha.trainstats.features.exercises.ui.history.ExerciseHistoryBottomSheet
import com.danilkha.trainstats.features.exercises.ui.history.ExerciseHistoryBottomSheetArgs
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun WorkoutScreenRoute(
    workoutId: String? = null,
    viewModel: WorkoutViewModel = koinViewModel(),
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState(viewModel.startState)

    LaunchedEffect(key1 = Unit) {
        viewModel.processEvent(WorkoutEvent.RequestInit(workoutId))
    }

    val exerciseSelectorViewModel = koinViewModel<ExerciseListViewModel>()

    val exerciseSelectorBottomSheet = rememberBottomSheetState(
        canHide = {
            exerciseSelectorViewModel.processEvent(ExerciseListEvent.OnSelectorClosed)
            true
        }
    )

    val exerciseEditorBottomSheet = rememberBottomSheetState(
        canHide = {
            exerciseSelectorViewModel.processEvent(ExerciseListEvent.OnSelectorClosed)
            true
        },
        onResult = {
            val result = it[ExerciseEditorBottomSheetArgs.result]
            if(result is ExerciseListEvent.UpdateList) {
                exerciseSelectorViewModel.processEvent(result)
            }
        }
    )

    viewModel.LaunchCollectEffects { event ->
        when (event) {
            WorkoutSideEffect.Deleted -> onSaved()
            WorkoutSideEffect.OpenExerciseSelector -> exerciseSelectorBottomSheet.show()
        }
    }

    var showDateDialog by rememberSaveable { mutableStateOf(false) }

    val exerciseHistoryBottomSheet = rememberBottomSheetState()
    exerciseSelectorViewModel.LaunchCollectEffects {
        when (it) {
            is ExerciseListSideEffect.ExerciseClicked -> {
                viewModel.processEvent(WorkoutEvent.AddExercise(it.exerciseModel))
                exerciseSelectorBottomSheet.hide()
            }
        }
    }


    if (showDateDialog) {
        DateSelector(
            onDismiss = { showDateDialog = false },
            onDateSelected = {
                viewModel.processEvent(WorkoutEvent.ChangeDate(it))
                showDateDialog = false
            }
        )
    }

    Box {
        WorkoutScreen(
            state = state,
            eventConsumer = viewModel::processEvent,
            onDateClicked = { showDateDialog = true },
            onSave = {
                viewModel.processEvent(WorkoutEvent.SaveWorkout)
                onSaved()
            },
            addExercise = {
                exerciseSelectorBottomSheet.show()
            },
            onDelete = { },
            onHistoryClick = {
                exerciseHistoryBottomSheet.show(ExerciseHistoryBottomSheetArgs.buildArgs(it))
            }
        )
        ExerciseHistoryBottomSheet(exerciseHistoryBottomSheet)
        ExerciseSelectorBottomSheet(
            sheetState = exerciseSelectorBottomSheet,
            openExerciseEditor = {
                exerciseEditorBottomSheet.show()
            }
        )
        ExerciseEditorBottomSheet(exerciseEditorBottomSheet)
    }
}

@Composable
fun WorkoutScreen(
    state: WorkoutState,
    eventConsumer: (WorkoutEvent) -> Unit,

    onDateClicked: () -> Unit,
    onHistoryClick: (String) -> Unit,
    addExercise: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = LocalDateFormat.current

    Column {
        TextToolbar(
            title = stringResource(when (state.initialization) {
                    WorkoutEditorInitialization.NEW -> Res.string.new_workout
                    WorkoutEditorInitialization.EDIT -> Res.string.edit_workout
                    null -> Res.string.new_workout
                }
            ),
            onBack = onSave
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(color = Colors.background)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDateClicked)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                text = dateFormat.format(state.date),
                style = ThemeTypography.title,
                color = Colors.primary
            )

            val dragDispatcher = remember { DragDispatcher() }

            DragAndDropColumn(
                items = state.groups,
                onItemMoved = { from, to ->
                    eventConsumer(WorkoutEvent.OnGroupMove(from, to))
                },
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
                        onWeightChange = { index, value ->
                            eventConsumer(
                                WorkoutEvent.EditWeight(
                                    groupIndex,
                                    index,
                                    value
                                )
                            )
                        },
                        onRepsChange = { index, side, value ->
                            eventConsumer(
                                WorkoutEvent.EditReps(
                                    groupIndex,
                                    index,
                                    side,
                                    value
                                )
                            )
                        },
                        onDelete = { eventConsumer(WorkoutEvent.DeleteSet(groupIndex, it)) },
                        onReturnDeleted = {
                            eventConsumer(
                                WorkoutEvent.ReturnDeletedSet(
                                    groupIndex,
                                    it
                                )
                            )
                        },
                        onSetMoved = { from, to ->
                            eventConsumer(
                                WorkoutEvent.OnSetMove(
                                    groupIndex,
                                    from,
                                    to
                                )
                            )
                        },
                        onExpandClick = { eventConsumer(WorkoutEvent.ToggleGroup(groupIndex)) },
                        separated = item.separated,
                        hasWeight = item.hasWeight,

                        onDragStart = { dragDispatcher.onDragStart(groupIndexUpdated) },
                        onDragEnd = dragDispatcher::onDragEnd,
                        onVerticalDrag = dragDispatcher::onDrag,
                        onDeleteGroup = { eventConsumer(WorkoutEvent.DeleteGroup(groupIndex)) },
                        onHistoryClick = { onHistoryClick(item.exerciseId) }
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
                    text = stringResource(Res.string.add_exercise),
                    color = Colors.secondary,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    color = Colors.secondary,
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            val lastEdited = state.lastEdited
            if (lastEdited != null) {
                Text(
                    text = "${stringResource(Res.string.last_edited)} ${dateFormat.format(lastEdited.toLocal())}",
                    style = MaterialTheme.typography.caption,
                    color = Colors.text.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val initialWorkout = state.initialWorkout
                val isSaved = initialWorkout != null && initialWorkout.saved
                if (isSaved) {
                    GenericButton(
                        onClick = onDelete,
                        color = Colors.error
                    ) {
                        Text(
                            stringResource(Res.string.delete),
                            color = Colors.textInverse
                        )
                    }
                }
                GenericButton(
                    onClick = onSave,
                ) {
                    Text(
                        text = if (isSaved) stringResource(Res.string.update)
                        else stringResource(Res.string.save)
                    )
                }
            }
        }
    }
}
