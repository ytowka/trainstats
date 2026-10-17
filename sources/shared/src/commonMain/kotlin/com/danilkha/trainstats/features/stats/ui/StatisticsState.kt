package com.danilkha.trainstats.features.stats.ui

import com.danilkha.trainstats.features.stats.domain.StatisticsData
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.stats.domain.StatisticsPoint
import com.danilkha.trainstats.features.stats.domain.exerciseStatistics
import com.danilkha.trainstats.features.stats.domain.maximumStatisticsPoints
import com.danilkha.trainstats.features.stats.domain.maximumStatisticsPoint
import com.danilkha.trainstats.features.stats.domain.FrequencyInterval
import com.danilkha.trainstats.features.stats.domain.FrequencyMetric
import com.danilkha.trainstats.features.stats.domain.FrequencyScope
import com.danilkha.trainstats.features.stats.domain.frequencyStatistics
import com.danilkha.commoncore.utils.toLocalDate
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/** Series zero is the maximum; actual approaches are numbered from one. */
const val MAXIMUM_SERIES = 0

enum class StatisticsTab { Metrics, Frequency }

data class StatisticsState(
    val data: StatisticsData = StatisticsData(emptyList(), emptyList()),
    val loading: Boolean = true,
    val failed: Boolean = false,
    val exerciseId: String? = null,
    val selectedVariationIds: Set<String?>? = null,
    val searchQuery: String = "",
    val metric: StatisticsMetric = StatisticsMetric.Weight,
    val selectedPoint: StatisticsPoint? = null,
    val visibleSeries: Set<Int> = setOf(MAXIMUM_SERIES),
    val tab: StatisticsTab = StatisticsTab.Metrics,
    val frequencyInterval: FrequencyInterval = FrequencyInterval.Week,
    val frequencyMetric: FrequencyMetric = FrequencyMetric.Workouts,
    val frequencyScope: FrequencyScope = FrequencyScope.Exercise,
    val frequencyToday: LocalDate = Clock.System.now().toLocalDate(),
    val frequencyDate: LocalDate? = null,
) {
    val frequency by lazy {
        frequencyStatistics(data.workouts, exerciseId, frequencyScope, frequencyInterval, frequencyToday, variationIds = selectedVariationIds)
    }
    val selectedFrequencyBucket get() = frequency.buckets.firstOrNull {
        frequencyDate?.let { date -> date >= it.start && date < it.endExclusive } == true
    } ?: frequency.buckets.last()
    val exercise get() = data.exercises.firstOrNull { it.id == exerciseId }
    val variationIds get() = setOf<String?>(null) + exercise?.variations.orEmpty().map { it.id }
    val activeVariationIds get() = selectedVariationIds ?: variationIds
    val needsVariationSelection get() = selectedVariationIds?.isEmpty() == true
    val filteredExercises get() = data.exercises.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val points: List<StatisticsPoint> by lazy {
        exerciseId?.let { exerciseStatistics(data.workouts, it, selectedVariationIds) }.orEmpty()
    }
    val selectedWorkout get() = data.workouts.firstOrNull { it.id == selectedPoint?.workoutId }
    val metricPoints by lazy {
        points.filter { metric.value(it)?.let { value -> value.isFinite() && value >= 0f } == true }
    }
    val maximumPoints by lazy { maximumStatisticsPoints(metricPoints, metric) }
    val visiblePoints by lazy {
        val maxima = if (MAXIMUM_SERIES in visibleSeries) maximumPoints.toSet() else emptySet()
        metricPoints.filter { it.setNumber in visibleSeries || it in maxima }
    }
    val recordPoint by lazy { maximumStatisticsPoint(metricPoints, metric) }
    val recordWorkout get() = data.workouts.firstOrNull { it.id == recordPoint?.workoutId }
}

sealed interface StatisticsEvent {
    data class SelectTab(val tab: StatisticsTab) : StatisticsEvent
    data class SelectFrequencyInterval(val interval: FrequencyInterval) : StatisticsEvent
    data class SelectFrequencyMetric(val metric: FrequencyMetric) : StatisticsEvent
    data class SelectFrequencyScope(val scope: FrequencyScope) : StatisticsEvent
    data class SelectFrequencyBucket(val date: LocalDate) : StatisticsEvent
    data object Retry : StatisticsEvent
    data class Loaded(val data: StatisticsData) : StatisticsEvent
    data object Failed : StatisticsEvent
    data class Search(val query: String) : StatisticsEvent
    data class SelectExercise(val id: String) : StatisticsEvent
    data class ToggleVariation(val id: String?) : StatisticsEvent
    data class SelectAllVariations(val checked: Boolean) : StatisticsEvent
    data class SelectMetric(val metric: StatisticsMetric) : StatisticsEvent
    data class SelectPoint(val point: StatisticsPoint) : StatisticsEvent
    data class ToggleSet(val number: Int) : StatisticsEvent
}
