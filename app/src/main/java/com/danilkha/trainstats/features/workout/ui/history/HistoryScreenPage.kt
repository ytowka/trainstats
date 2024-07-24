package com.danilkha.trainstats.features.workout.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.format
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.Fab
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography

@Composable
fun HistoryScreenPage(
    viewModel: HistoryViewModel = getCurrentViewModel { it.historyViewModel },
    onWorkoutClicked: (id: Long) -> Unit,
    onAddClicked: () -> Unit
){
    val state by viewModel.state.collectAsState()

    HistoryPage(
        state = state,
        onWorkoutClicked = { onWorkoutClicked(it.id) },
        onAddClicked = onAddClicked
    )
}

@Composable
fun HistoryPage(
    state: HistoryState,
    onWorkoutClicked: (WorkoutHistoryModel) -> Unit,
    onAddClicked: () -> Unit,
){

    Box(
        modifier = Modifier.fillMaxSize(),
    ){
        Column {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 15.dp
                )
            ) {
                Text(
                    text = stringResource(id = R.string.history),
                    style = ThemeTypography.title
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(items = state.workouts, key = { it.id }){
                    WorkoutCard(
                        workout = it,
                        onClick = { onWorkoutClicked(it) }
                    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutCard(
    workout: WorkoutHistoryModel,
    onClick: () -> Unit,
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = workout.date.date.format(),
            style = ThemeTypography.subtitle.copy(
                color = Colors.primary
            )
        )
        Spacer(modifier = Modifier.size(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            workout.exercises.forEach {
                ExerciseChip(it)
            }
        }
    }
}

@Composable
fun ExerciseChip(
    label: String,
) {
    Text(
        modifier = Modifier
            .background(
                color = Colors.secondary,
                shape = RoundedCornerShape(50)
            )
            .padding(vertical = 5.dp, horizontal = 16.dp),
        text = label,
        color = Colors.textInverse
    )
}