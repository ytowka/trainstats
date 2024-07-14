package com.danilkha.trainstats.features.exercises.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheet
import com.danilkha.uikit.bottomsheet.rememberBottomSheetController
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.Fab

@Composable
fun ExerciseListScreenPage(
    viewModel: ExerciseListViewModel = getCurrentViewModel { it.exerciseListViewModel }
) {
    val exerciseEditor = rememberBottomSheetController(bottomSheetClass = ExerciseEditorBottomSheet::class.java)

    val state by viewModel.state.collectAsState()
    ExerciseListScreen(
        state = state,
        onAddClicked = {exerciseEditor.show()}
    )
}

@Composable
fun ExerciseListScreen(
    state: ExerciseListState,
    onAddClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ){
        Column {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 10.dp,
                    bottom = 60.dp
                ),
            ) {
                items(items = state.exerciseList, key = { it.id }){
                    ExerciseCard(it)
                }
            }
        }
        Fab(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomEnd),
            onClick = onAddClicked
        )
    }
}

@Composable
fun ExerciseCard(
    exerciseModel: ExerciseModel
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Text(
            text = exerciseModel.name,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if(exerciseModel.separated){
                Text(
                    text = stringResource(id = com.danilkha.uikit.R.string.split),
                    style = MaterialTheme.typography.body2
                )
            }
            Text(
                text = if(exerciseModel.hasWeight){
                    stringResource(id = com.danilkha.uikit.R.string.has_weight)
                }else stringResource(id = com.danilkha.uikit.R.string.with_body_weight),
                style = MaterialTheme.typography.body2
            )
        }
    }
}
