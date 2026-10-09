package com.danilkha.trainstats.features.stats.ui

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.stats.domain.GetStatisticsUseCase
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.stats.domain.exerciseStatistics
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class StatisticsViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase,
) : MviViewModel<StatisticsState, StatisticsEvent, Nothing>() {
    override val startState = StatisticsState()
    private var loadJob: Job? = null

    override suspend fun loadData() = reload()

    private fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = getStatisticsUseCase(Unit)
            coroutineContext.ensureActive()
            processEvent(result.fold(StatisticsEvent::Loaded, { StatisticsEvent.Failed }))
        }
    }

    override fun reduce(state: StatisticsState, event: StatisticsEvent): StatisticsState = when (event) {
        StatisticsEvent.Retry -> state.copy(loading = true, failed = false)
        StatisticsEvent.Failed -> state.copy(loading = false, failed = true)
        is StatisticsEvent.Loaded -> {
            val exercise = event.data.exercises.firstOrNull { it.id == state.exerciseId }
                ?: event.data.exercises.firstOrNull { exercise ->
                    event.data.workouts.any { workout -> workout.steps.any { it.exerciseData.id == exercise.id } }
                } ?: event.data.exercises.firstOrNull()
            state.copy(
                data = event.data, loading = false, failed = false, exerciseId = exercise?.id,
                hiddenSets = if (exercise?.id == state.exerciseId) state.hiddenSets else emptySet(),
                metric = if (exercise?.hasWeight == false) StatisticsMetric.Repetitions else state.metric,
                selectedPoint = state.selectedPoint?.let { selected ->
                    exercise?.id?.let { exerciseStatistics(event.data.workouts, it) }
                        ?.firstOrNull { it.workoutId == selected.workoutId && it.setNumber == selected.setNumber }
                },
            )
        }
        is StatisticsEvent.Search -> state.copy(searchQuery = event.query)
        is StatisticsEvent.SelectExercise -> state.copy(
            exerciseId = event.id, searchQuery = "", selectedPoint = null, hiddenSets = emptySet(),
            metric = if (state.data.exercises.firstOrNull { it.id == event.id }?.hasWeight == false)
                StatisticsMetric.Repetitions else state.metric,
        )
        is StatisticsEvent.SelectMetric -> state.copy(metric = event.metric, selectedPoint = null)
        is StatisticsEvent.SelectPoint -> state.copy(selectedPoint = event.point)
        is StatisticsEvent.ToggleSet -> state.copy(
            hiddenSets = if (event.number in state.hiddenSets) state.hiddenSets - event.number else state.hiddenSets + event.number,
            selectedPoint = state.selectedPoint?.takeUnless { it.setNumber == event.number },
        )
    }

    override suspend fun afterReduce(newState: StatisticsState, event: StatisticsEvent) {
        if (event == StatisticsEvent.Retry) reload()
    }
}
