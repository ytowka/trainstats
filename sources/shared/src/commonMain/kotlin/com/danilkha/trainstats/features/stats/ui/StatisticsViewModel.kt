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
        is StatisticsEvent.ToggleVariation -> state.copy(
            selectedVariationIds = if (event.id in state.activeVariationIds) state.activeVariationIds - event.id else state.activeVariationIds + event.id,
            selectedPoint = null, frequencyDate = null,
        )
        is StatisticsEvent.SelectAllVariations -> state.copy(
            selectedVariationIds = if (event.checked) null else emptySet(), selectedPoint = null, frequencyDate = null,
        )
        is StatisticsEvent.SelectTab -> state.copy(tab = event.tab)
        is StatisticsEvent.SelectFrequencyInterval -> state.copy(frequencyInterval = event.interval)
        is StatisticsEvent.SelectFrequencyMetric -> state.copy(frequencyMetric = event.metric)
        is StatisticsEvent.SelectFrequencyScope -> state.copy(frequencyScope = event.scope, frequencyDate = null)
        is StatisticsEvent.SelectFrequencyBucket -> state.copy(frequencyDate = event.date)
        StatisticsEvent.Retry -> state.copy(loading = true, failed = false)
        StatisticsEvent.Failed -> state.copy(loading = false, failed = true)
        is StatisticsEvent.Loaded -> {
            val exercise = event.data.exercises.firstOrNull { it.id == state.exerciseId }
                ?: event.data.exercises.firstOrNull { exercise ->
                    event.data.workouts.any { workout -> workout.steps.any { it.exerciseData.id == exercise.id } }
                } ?: event.data.exercises.firstOrNull()
            val selectedVariations = if (exercise?.id == state.exerciseId && !exercise?.variations.isNullOrEmpty())
                state.selectedVariationIds?.intersect(setOf<String?>(null) + exercise.variations.map { it.id }) else null
            state.copy(
                data = event.data, loading = false, failed = false, exerciseId = exercise?.id,
                selectedVariationIds = selectedVariations,
                visibleSeries = if (exercise?.id == state.exerciseId) state.visibleSeries else setOf(MAXIMUM_SERIES),
                metric = if (exercise?.hasWeight == false) StatisticsMetric.Repetitions else state.metric,
                selectedPoint = state.selectedPoint?.let { selected ->
                    exercise?.id?.let { exerciseStatistics(event.data.workouts, it, selectedVariations) }
                        ?.firstOrNull { it.workoutId == selected.workoutId && it.setNumber == selected.setNumber }
                },
            )
        }
        is StatisticsEvent.Search -> state.copy(searchQuery = event.query)
        is StatisticsEvent.SelectExercise -> state.copy(
            selectedVariationIds = if (event.id == state.exerciseId) state.selectedVariationIds else null,
            exerciseId = event.id, searchQuery = "", selectedPoint = null, visibleSeries = setOf(MAXIMUM_SERIES),
            metric = if (state.data.exercises.firstOrNull { it.id == event.id }?.hasWeight == false)
                StatisticsMetric.Repetitions else state.metric,
        )
        is StatisticsEvent.SelectMetric -> state.copy(metric = event.metric, selectedPoint = null)
        is StatisticsEvent.SelectPoint -> state.copy(selectedPoint = event.point)
        is StatisticsEvent.ToggleSet -> {
            val changed = state.copy(
                visibleSeries = if (event.number in state.visibleSeries) state.visibleSeries - event.number else state.visibleSeries + event.number,
            )
            changed.copy(selectedPoint = state.selectedPoint?.takeIf { it in changed.visiblePoints })
        }
    }

    override suspend fun afterReduce(newState: StatisticsState, event: StatisticsEvent) {
        if (event == StatisticsEvent.Retry) reload()
    }
}
