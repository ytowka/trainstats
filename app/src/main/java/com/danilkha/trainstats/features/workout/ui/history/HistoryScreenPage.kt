package com.danilkha.trainstats.features.workout.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.LocalDateFormat
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.Fab
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.PreviewContent
import com.danilkha.commonds.theme.ThemeTypography
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import training_stats.common_ds.generated.resources.Res
import training_stats.common_ds.generated.resources.*
import kotlin.time.Clock

@Composable
fun HistoryScreenPage(
    viewModel: HistoryViewModel = getCurrentViewModel { it.historyViewModel },
    onWorkoutClicked: (id: Long) -> Unit,
    onAddClicked: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    HistoryPage(
        state = state,
        onWorkoutClicked = { onWorkoutClicked(it.id) },
        onCalendarClicked = { },
        onSearchQueryChanged = { viewModel.processEvent(HistoryEvent.ChangeSearchQuery(it)) },
        onAddClicked = onAddClicked,
    )
}

@Composable
fun HistoryPage(
    state: HistoryState,
    onWorkoutClicked: (WorkoutHistoryModel) -> Unit,
    onCalendarClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
) {
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    HistoryPageLayout(
        topBar = {
            TopBar(
                onCalendarClicked = onCalendarClicked,
                searchQuery = state.searchQuery,
                onAddClicked = onAddClicked,
                onSearchQueryChanged = onSearchQueryChanged,
            )
        },
        workoutList = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(items = state.workouts, key = { it.id }) {
                    WorkoutCard(
                        workout = it,
                        onClick = { onWorkoutClicked(it) }
                    )
                }
            }
        },
        isScrolled = isScrolled,
        onScrollToTop = {
            listState.animateScrollToItem(0)
        }
    )
}

@Composable
fun HistoryPageLayout(
    topBar: @Composable ColumnScope.() -> Unit,
    workoutList: @Composable ColumnScope.() -> Unit,
    isScrolled: Boolean,
    onScrollToTop: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            topBar()
            workoutList()
        }
        AnimatedVisibility(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomEnd),
            visible = isScrolled,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
        ) {
            Fab(
                icon =  rememberVectorPainter(Icons.Default.KeyboardArrowUp),
                onClick = {
                    scope.launch {
                        onScrollToTop()
                    }
                }
            )
        }
    }
}

@Composable
fun TopBar(
    onCalendarClicked: () -> Unit,
    onAddClicked: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Card(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GenericTextFiled(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            hint = stringResource(id = R.string.history),
            contentStart = {
                Icon(imageVector = Icons.Default.Search)
            },
            contentEnd = if(searchQuery.isNotEmpty()) {
                {
                    Icon(
                        imageVector = Icons.Default.Close,
                        onClick = {
                            onSearchQueryChanged("")
                            focusManager.clearFocus()
                        }
                    )
                }
            } else null
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LargeButton(
                painter = painterResource(Res.drawable.ic_add),
                onClick = onAddClicked
            )
            LargeButton(
                painter = painterResource(Res.drawable.ic_chart),
                onClick = {}
            )
            LargeButton(
                painter = rememberVectorPainter(Icons.Default.CalendarMonth),
                onClick = onCalendarClicked
            )
        }
    }
}

@Composable
private fun LargeButton(
    painter: Painter,
    onClick: () -> Unit,
) {
    Card(
        backgroundColor = Colors.background,
        onClick = onClick
    ) {
        androidx.compose.material.Icon(
            painter = painter,
            contentDescription = null
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutHistoryModel,
    onClick: () -> Unit,
) {
    val dateFormat = LocalDateFormat.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = dateFormat.format(workout.date.toLocal().date),
            style = ThemeTypography.subtitle.copy(
                color = Colors.primary,
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.size(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),

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
            .padding(vertical = 5.dp, horizontal = 12.dp),
        text = label,
        color = Colors.textInverse,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
@Preview
fun HistoryPagePreview() {
    PreviewContent {
        HistoryPage(
            state = HistoryState(
                allWorkouts = listOf(
                    WorkoutHistoryModel(
                        id = 0,
                        date = Clock.System.now(),
                        exercises = listOf("becnh press")
                    )
                ),
            ),
            onWorkoutClicked = {},
            onAddClicked = {},
            onCalendarClicked = {},
            onSearchQueryChanged = {}
        )
    }
}
