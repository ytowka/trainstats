package com.danilkha.trainstats.features.stats.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.danilkha.commoncore.utils.format2
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.navigation.api.LocalNavigator
import com.danilkha.navigation.api.destinations.WorkoutDetailsNav
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.workout.ui.history.WorkoutCard
import com.danilkha.trainstats.features.workout.ui.history.WorkoutHistoryModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel()) {
    val stateFlow = remember(viewModel) { viewModel.state }
    val state by stateFlow.collectAsState()
    val navigator = LocalNavigator.current
    StatisticsPage(
        state = state,
        onEvent = viewModel::processEvent,
        onBack = { navigator.back() },
        onWorkout = { navigator.navigate(WorkoutDetailsNav(it)) },
    )
}

@Composable
fun StatisticsPage(
    state: StatisticsState,
    onEvent: (StatisticsEvent) -> Unit,
    onBack: () -> Unit,
    onWorkout: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TextToolbar(title = stringResource(Res.string.navigation_item_stats), onBack = onBack)
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colors.primary)
            }
            state.failed -> StatisticsError(onRetry = { onEvent(StatisticsEvent.Retry) })
            state.data.exercises.isEmpty() -> Text(
                stringResource(Res.string.stats_no_exercises),
                modifier = Modifier.padding(20.dp), color = Colors.text,
            )
            else -> {
                val points = remember(state.points, state.metric) {
                    state.points.filter { state.metric.value(it)?.let { value -> value.isFinite() && value >= 0f } == true }
                }
                BoxWithConstraints(Modifier.weight(1f)) {
                    val listState = rememberLazyListState()
                    val chartHeight = (maxHeight * .5f).coerceIn(220.dp, 340.dp)
                    LaunchedEffect(state.selectedPoint) {
                        if (state.selectedPoint != null) listState.animateScrollToItem(3)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Card(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).testTag("statistics_filters"), contentPadding = PaddingValues()) {
                                ExerciseDropdown(state, onEvent)
                                Divider(color = Colors.outline)
                                MetricDropdown(state.metric) { onEvent(StatisticsEvent.SelectMetric(it)) }
                            }
                        }
                        item {
                            when {
                                state.points.isEmpty() -> Text(stringResource(Res.string.stats_no_history), color = Colors.text)
                                points.isEmpty() -> Text(stringResource(Res.string.stats_no_weight), color = Colors.text)
                                else -> key(state.exerciseId, state.metric) {
                                    StatisticsChart(
                                        points = points,
                                        metric = state.metric,
                                        selectedPoint = state.selectedPoint,
                                        chartHeight = chartHeight,
                                        hiddenSets = state.hiddenSets,
                                        onToggleSet = { onEvent(StatisticsEvent.ToggleSet(it)) },
                                        onSelect = { onEvent(StatisticsEvent.SelectPoint(it)) },
                                    )
                                }
                            }
                        }
                        if (points.isNotEmpty()) {
                            item {
                                val visiblePoints = state.visiblePoints
                                val selectedIndex = visiblePoints.indexOf(state.selectedPoint)
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        enabled = visiblePoints.isNotEmpty(),
                                        onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints.last())) },
                                        modifier = Modifier.weight(1f),
                                    ) { Text(stringResource(Res.string.stats_latest_point)) }
                                    IconButton(
                                        enabled = visiblePoints.isNotEmpty() && selectedIndex != 0,
                                        onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints[if (selectedIndex < 0) visiblePoints.lastIndex else selectedIndex - 1])) },
                                    ) {
                                        androidx.compose.material.Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(Res.string.stats_previous_point))
                                    }
                                    IconButton(
                                        enabled = selectedIndex in 0 until visiblePoints.lastIndex,
                                        onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints[selectedIndex + 1])) },
                                    ) {
                                        androidx.compose.material.Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(Res.string.stats_next_point))
                                    }
                                }
                            }
                        }
                        state.selectedWorkout?.let { workout ->
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    state.selectedPoint?.let { point ->
                                        Text(
                                            text = stringResource(Res.string.stats_point_value, point.setNumber, state.metric.value(point)?.format2().orEmpty(), metricUnit(state.metric)),
                                            color = setColor(point.setNumber), style = ThemeTypography.body1,
                                        )
                                    }
                                    WorkoutCard(
                                        workout = WorkoutHistoryModel(
                                            id = workout.id, date = workout.dateTime,
                                            exercises = workout.steps.sortedBy { it.orderPosition }.map { it.exerciseData.name }.distinct(),
                                        ),
                                        showTime = true,
                                        onClick = { onWorkout(workout.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDropdown(state: StatisticsState, onEvent: (StatisticsEvent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var widthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().onSizeChanged { widthPx = it.width }) {
        Column(Modifier.fillMaxWidth().testTag("statistics_exercise_dropdown").clickable { expanded = true }.padding(16.dp)) {
            Text(stringResource(Res.string.stats_exercise), style = ThemeTypography.body2.copy(fontSize = 12.sp), color = Colors.text.copy(alpha = .6f))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(state.exercise?.name ?: stringResource(Res.string.stats_choose_exercise), Modifier.weight(1f), style = ThemeTypography.subtitle, color = Colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Icon(imageVector = Icons.Default.KeyboardArrowDown)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; onEvent(StatisticsEvent.Search("")) },
            modifier = Modifier.heightIn(max = 380.dp).width(with(density) { widthPx.toDp() }).testTag("statistics_exercise_menu"),
        ) {
            GenericTextFiled(
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                value = state.searchQuery,
                onValueChange = { onEvent(StatisticsEvent.Search(it)) },
                hint = stringResource(Res.string.search),
            )
            if (state.filteredExercises.isEmpty()) {
                Text(stringResource(Res.string.empty_search), Modifier.padding(16.dp), color = Colors.text)
            }
            state.filteredExercises.forEach { exercise ->
                DropdownMenuItem(onClick = {
                    onEvent(StatisticsEvent.SelectExercise(exercise.id))
                    expanded = false
                }) { Text(exercise.name, color = if (exercise.id == state.exerciseId) Colors.primary else Colors.text) }
            }
        }
    }
}

@Composable
private fun MetricDropdown(metric: StatisticsMetric, onSelect: (StatisticsMetric) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var widthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().onSizeChanged { widthPx = it.width }) {
        Row(Modifier.fillMaxWidth().testTag("statistics_metric_dropdown").clickable { expanded = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.stats_metric), Modifier.weight(1f), style = ThemeTypography.body2, color = Colors.text.copy(alpha = .6f))
            Text(metricLabel(metric), style = ThemeTypography.body1, color = Colors.primary)
            Spacer(Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.KeyboardArrowDown, color = Colors.primary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(with(density) { widthPx.toDp() }).testTag("statistics_metric_menu")) {
            StatisticsMetric.entries.forEach { option ->
                DropdownMenuItem(onClick = { onSelect(option); expanded = false }) {
                    Text(metricLabel(option), color = Colors.text)
                }
            }
        }
    }
}

@Composable
internal fun metricLabel(metric: StatisticsMetric): String = stringResource(when (metric) {
    StatisticsMetric.Weight -> Res.string.weight
    StatisticsMetric.Repetitions -> Res.string.reps_label
    StatisticsMetric.Volume -> Res.string.stats_volume
})

@Composable
internal fun metricUnit(metric: StatisticsMetric): String = stringResource(when (metric) {
    StatisticsMetric.Weight -> Res.string.kg
    StatisticsMetric.Repetitions -> Res.string.reps
    StatisticsMetric.Volume -> Res.string.stats_volume_unit
})

@Composable
internal fun StatisticsError(onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(Res.string.stats_load_failed), color = Colors.text)
        TextButton(onClick = onRetry) { Text(stringResource(Res.string.stats_retry)) }
    }
}
