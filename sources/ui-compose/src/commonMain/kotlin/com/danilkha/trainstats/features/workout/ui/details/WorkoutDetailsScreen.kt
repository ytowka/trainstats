package com.danilkha.trainstats.features.workout.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import com.danilkha.commoncore.utils.LocalDateFormat
import com.danilkha.commoncore.utils.format2
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.navigation.api.LocalNavigator
import com.danilkha.trainstats.features.stats.ui.StatisticsError
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun WorkoutDetailsScreen(workoutId: String, viewModel: WorkoutDetailsViewModel = koinViewModel()) {
    val stateFlow = remember(viewModel) { viewModel.state }
    val state by stateFlow.collectAsState()
    val navigator = LocalNavigator.current
    val dateFormat = LocalDateFormat.current
    LaunchedEffect(workoutId) { viewModel.processEvent(WorkoutDetailsEvent.Load(workoutId)) }
    Column(Modifier.fillMaxSize()) {
        TextToolbar(stringResource(Res.string.stats_workout_details), onBack = { navigator.back() })
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colors.primary)
            }
            state.failed -> StatisticsError { viewModel.processEvent(WorkoutDetailsEvent.Load(workoutId)) }
            else -> state.workout?.let { workout ->
                val groups = remember(workout) {
                    val result = mutableListOf<MutableList<ExerciseSet>>()
                    workout.steps.sortedBy { it.orderPosition }.forEach { set ->
                        if (result.lastOrNull()?.lastOrNull()?.exerciseData?.id != set.exerciseData.id) result.add(mutableListOf())
                        result.last().add(set)
                    }
                    result
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(dateFormat.format(workout.dateTime.toLocal()), color = Colors.primary, style = ThemeTypography.subtitle)
                    }
                    itemsIndexed(groups) { _, sets ->
                        Card(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(sets.first().exerciseData.name, color = Colors.text, style = ThemeTypography.title)
                            sets.forEach { set -> ReadOnlySetRow(set) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlySetRow(set: ExerciseSet) {
    val style = ThemeTypography.body1.copy(fontWeight = FontWeight.Normal)
    val numberStyle = SpanStyle(fontSize = (style.fontSize.value + 2).sp, fontWeight = FontWeight.Bold)
    val kg = stringResource(Res.string.kg)
    val repsUnit = stringResource(Res.string.reps)
    val leftShort = stringResource(Res.string.stats_left_short)
    val rightShort = stringResource(Res.string.stats_right_short)
    Row(
        Modifier.fillMaxWidth().background(Colors.background, RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        set.weight?.let { weight ->
            Text(buildAnnotatedString {
                withStyle(numberStyle) { append(weight.value.format2()) }
                append(" $kg")
            }, color = Colors.text, style = style)
        }
        Text(buildAnnotatedString {
            when (val reps = set.reps) {
                is Repetitions.Single -> withStyle(numberStyle) { append(reps.reps.format2()) }
                is Repetitions.Double -> {
                    append(leftShort)
                    withStyle(numberStyle) { append(reps.left.format2()) }
                    append(" · ")
                    append(rightShort)
                    withStyle(numberStyle) { append(reps.right.format2()) }
                }
            }
            append(" $repsUnit")
        }, color = Colors.text, style = style)
    }
}
