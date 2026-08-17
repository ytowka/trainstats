package com.danilkha.trainstats.features.exercises.ui.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import com.danilkha.commonds.bottomsheet.BottomSheetScreen
import com.danilkha.commonds.bottomsheet.BottomSheetState
import com.danilkha.trainstats.features.exercises.ui.ExerciseList
import com.danilkha.trainstats.features.exercises.ui.ExerciseListEvent
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.ExerciseSearchBar
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.theme.Colors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun ExerciseSelectorBottomSheet(
    sheetState: BottomSheetState,
    openExerciseEditor: () -> Unit,
) {
    val viewModel: ExerciseListViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    BottomSheetScreen(
        state = sheetState
    ) {
        BottomSheetContent(
            title = stringResource(Res.string.add_exercise),
            onCloseClicked = { sheetState.hide() }
        ) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = Colors.background)
                    .fillMaxSize()
            ) {
                ExerciseSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.processEvent(ExerciseListEvent.ChangeSearchQuery(it)) },
                    focusRequester = focusRequester,
                    onAddClicked = {
                        openExerciseEditor()
                    }
                )
                ExerciseList(
                    items = state.exerciseList,
                    onClick = { viewModel.processEvent(ExerciseListEvent.OnExerciseClicked(it)) },
                )
            }
        }
    }
}
