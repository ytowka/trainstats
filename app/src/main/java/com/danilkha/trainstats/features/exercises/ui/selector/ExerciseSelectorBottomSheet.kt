package com.danilkha.trainstats.features.exercises.ui.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.exercises.ui.ExerciseList
import com.danilkha.trainstats.features.exercises.ui.ExerciseListScreen
import com.danilkha.trainstats.features.exercises.ui.ExerciseListViewModel
import com.danilkha.trainstats.features.exercises.ui.ExerciseSearchBar
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheet
import com.danilkha.uikit.bottomsheet.ComposeContextBottomDialog
import com.danilkha.uikit.bottomsheet.rememberBottomSheetController
import com.danilkha.uikit.components.BottomSheetContent
import com.danilkha.uikit.theme.Colors

class ExerciseSelectorBottomSheet : ComposeContextBottomDialog(){

    override val content: @Composable () -> Unit = {

        val viewModel: ExerciseListViewModel = getCurrentViewModel { it.exerciseListViewModel }

        val exerciseEditor = rememberBottomSheetController(bottomSheetClass = ExerciseEditorBottomSheet::class.java)

        val state by viewModel.state.collectAsState()
        BottomSheetContent(
            title = stringResource(id = R.string.add_exercise),
            onCloseClicked = { dismiss() }
        ) {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = Colors.background)
                    .fillMaxSize()
                ) {
                ExerciseSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::queryUpdated,
                    onAddClicked = {
                        exerciseEditor.show()
                    }
                )
                ExerciseList(
                    items = state.exerciseList,
                    onClick =  viewModel::onExerciseClicked,
                )
            }
        }
    }

}