package com.danilkha.trainstats.features.stats.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
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
import com.danilkha.trainstats.features.stats.domain.StatisticsPoint
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
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
                val points = state.metricPoints
                BoxWithConstraints(Modifier.weight(1f)) {
                    val listState = rememberLazyListState()
                    val chartHeight = (maxHeight * .5f).coerceIn(220.dp, 340.dp)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 40.dp),
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
                                        visibleSeries = state.visibleSeries,
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
                                val previousEnabled = visiblePoints.isNotEmpty() && selectedIndex != 0
                                val nextEnabled = selectedIndex in 0 until visiblePoints.lastIndex
                                Card(Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        TextButton(
                                            enabled = visiblePoints.isNotEmpty(),
                                            onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints.last())) },
                                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                                .background(Colors.primary.copy(alpha = .08f), RoundedCornerShape(8.dp)),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Colors.primary,
                                                disabledContentColor = Colors.text.copy(alpha = .3f),
                                            ),
                                        ) {
                                            Text(
                                                stringResource(Res.string.stats_latest_point),
                                                style = ThemeTypography.body2.copy(color = Color.Unspecified, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
                                            )
                                        }
                                        IconButton(
                                            enabled = previousEnabled,
                                            onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints[if (selectedIndex < 0) visiblePoints.lastIndex else selectedIndex - 1])) },
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Colors.background),
                                        ) {
                                            androidx.compose.material.Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                stringResource(Res.string.stats_previous_point),
                                                tint = Colors.primary.copy(alpha = if (previousEnabled) 1f else .3f),
                                            )
                                        }
                                        IconButton(
                                            enabled = nextEnabled,
                                            onClick = { onEvent(StatisticsEvent.SelectPoint(visiblePoints[selectedIndex + 1])) },
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Colors.background),
                                        ) {
                                            androidx.compose.material.Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                stringResource(Res.string.stats_next_point),
                                                tint = Colors.primary.copy(alpha = if (nextEnabled) 1f else .3f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        state.selectedWorkout?.let { workout ->
                            item(key = "selected_workout") {
                                Card(
                                    Modifier.fillMaxWidth().testTag("statistics_selected_workout"),
                                    contentPadding = PaddingValues(),
                                    backgroundColor = Colors.background,
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { onWorkout(workout.id) },
                                ) {
                                    state.selectedPoint?.let { point ->
                                        val color = if (MAXIMUM_SERIES in state.visibleSeries && point in state.maximumPoints)
                                            Colors.primary else setColor(point.setNumber)
                                        PointValueHeader(point, color)
                                    }
                                    StatisticsWorkoutCard(workout)
                                }
                            }
                        }
                        state.recordPoint?.let { record ->
                            state.recordWorkout?.let { workout ->
                                item(key = "record") {
                                    Card(
                                        Modifier.fillMaxWidth().testTag("statistics_record_workout"),
                                        contentPadding = PaddingValues(),
                                        backgroundColor = Colors.background,
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = { onWorkout(workout.id) },
                                    ) {
                                        Card(
                                            Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp),
                                            backgroundColor = Colors.primary.copy(alpha = .08f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                                            strokeColor = null,
                                        ) {
                                            Text(stringResource(Res.string.stats_all_time_record), style = ThemeTypography.body2, color = Colors.text.copy(alpha = .65f))
                                            Text(
                                                fullApproachValue(record, 28.sp),
                                                modifier = Modifier.testTag("statistics_record_value"),
                                                color = Colors.primary, style = ThemeTypography.body2.copy(fontSize = 16.sp),
                                            )
                                            Text(
                                                stringResource(Res.string.stats_set_number, record.setNumber) +
                                                    if (state.metric == StatisticsMetric.Volume) " · ${record.volume!!.format2()} ${metricUnit(state.metric)}" else "",
                                                style = ThemeTypography.body2, color = Colors.text.copy(alpha = .65f),
                                            )
                                        }
                                        StatisticsWorkoutCard(workout)
                                    }
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
private fun PointValueHeader(point: StatisticsPoint, color: Color) {
    val label = stringResource(Res.string.stats_set_number, point.setNumber)
    val value = fullApproachValue(point, 20.sp)
    Row(
        Modifier.fillMaxWidth()
            .background(color.copy(alpha = .08f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.width(3.dp).height(28.dp).background(color, RoundedCornerShape(50)))
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = Colors.text.copy(alpha = .65f), fontSize = 14.sp)) { append("$label · ") }
                append(value)
            },
            modifier = Modifier.testTag("statistics_selected_point"),
            color = color, style = ThemeTypography.body2.copy(fontSize = 14.sp),
        )
    }
}

@Composable
private fun fullApproachValue(point: StatisticsPoint, numberSize: TextUnit): AnnotatedString {
    val numberStyle = SpanStyle(fontSize = numberSize, fontWeight = FontWeight.Bold)
    val kg = stringResource(Res.string.kg)
    val repsUnit = stringResource(Res.string.reps)
    val left = stringResource(Res.string.stats_left_short).trimEnd()
    val right = stringResource(Res.string.stats_right_short).trimEnd()
    return buildAnnotatedString {
        point.weight?.let { weight ->
            withStyle(numberStyle) { append(weight.format2()) }
            append(" $kg × ")
        }
        when (val reps = point.approachRepetitions) {
            is Repetitions.Single -> withStyle(numberStyle) { append(reps.reps.format2()) }
            is Repetitions.Double -> {
                append("$left ")
                withStyle(numberStyle) { append(reps.left.format2()) }
                append(" · $right ")
                withStyle(numberStyle) { append(reps.right.format2()) }
            }
        }
        append(" $repsUnit")
    }
}

@Composable
private fun StatisticsWorkoutCard(workout: Workout) {
    WorkoutCard(
        workout = WorkoutHistoryModel(
            id = workout.id, date = workout.dateTime,
            exercises = workout.steps.sortedBy { it.orderPosition }.map { it.exerciseData.name }.distinct(),
        ),
        showTime = false,
        onClick = null,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        strokeColor = null,
    )
}

@Composable
private fun SelectionDropdownField(title: String, value: String, expanded: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "Dropdown arrow")
    Column(modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)) {
        Text(title, style = ThemeTypography.body2.copy(fontSize = 12.sp), color = Colors.text.copy(alpha = .6f))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, Modifier.weight(1f), style = ThemeTypography.subtitle, color = Colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Icon(modifier = Modifier.rotate(arrowRotation), imageVector = Icons.Default.KeyboardArrowDown)
        }
    }
}

@Composable
private fun ExerciseDropdown(state: StatisticsState, onEvent: (StatisticsEvent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var widthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val menuScrollState = rememberScrollState()
    LaunchedEffect(expanded, state.searchQuery) {
        if (expanded) menuScrollState.scrollTo(0)
    }
    Box(Modifier.fillMaxWidth().onSizeChanged { widthPx = it.width }) {
        SelectionDropdownField(
            title = stringResource(Res.string.stats_exercise),
            value = state.exercise?.name ?: stringResource(Res.string.stats_choose_exercise),
            expanded = expanded,
            modifier = Modifier.testTag("statistics_exercise_dropdown"),
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; onEvent(StatisticsEvent.Search("")) },
            modifier = Modifier.heightIn(max = 380.dp).width(with(density) { widthPx.toDp() }).testTag("statistics_exercise_menu"),
            scrollState = menuScrollState,
        ) {
            // Counter the menu's scroll offset so the opaque search header stays at the top.
            Box(
                Modifier.fillMaxWidth().offset { IntOffset(0, menuScrollState.value) }
                    .zIndex(1f).background(MaterialTheme.colors.surface).padding(10.dp),
            ) {
                GenericTextFiled(
                    modifier = Modifier.fillMaxWidth().testTag("statistics_exercise_search"),
                    value = state.searchQuery,
                    onValueChange = { onEvent(StatisticsEvent.Search(it)) },
                    hint = stringResource(Res.string.search),
                )
            }
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
        SelectionDropdownField(
            title = stringResource(Res.string.stats_metric),
            value = metricLabel(metric),
            expanded = expanded,
            modifier = Modifier.testTag("statistics_metric_dropdown"),
            onClick = { expanded = true },
        )
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
