package com.danilkha.trainstats.features.workout.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.room.Query
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.format
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.Fab
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.GenericTextFiled
import com.danilkha.uikit.components.Icon
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
        onCalendarClicked = {  },
        onSearchClicked = {  },
        onSearchQueryChanged = {  },
        onAddClicked = onAddClicked,
    )
}

@Composable
fun HistoryPage(
    state: HistoryState,
    onWorkoutClicked: (WorkoutHistoryModel) -> Unit,
    onCalendarClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
){

    Box(
        modifier = Modifier.fillMaxSize(),
    ){
        Column {
            TopBar(
                onCalendarClicked = onCalendarClicked,
                searchQuery = state.searchQuery,
                onSearchClicked = onSearchClicked,
                onSearchQueryChanged = onSearchQueryChanged,
            )
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

@Composable
fun TopBar(
    onCalendarClicked: () -> Unit,
    searchQuery: String,
    onSearchClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        val topPadding = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this).toDp()
        }
        Row(
            modifier = Modifier
                .padding(top = topPadding)
                .height(IntrinsicSize.Min)
        ){
            /*Text(
                modifier = Modifier.padding(start = 10.dp),
                text = stringResource(id = R.string.history),
                style = ThemeTypography.title
            )*/
            GenericTextFiled(
                modifier = Modifier.weight(1f),
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                hint = stringResource(id = R.string.history),
                contentStart ={
                    Icon(imageVector = Icons.Default.Search)
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            GenericButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentPaddings = PaddingValues(0.dp),
                color = Colors.background,
                onClick = onCalendarClicked
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth
                )
            }
        }
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