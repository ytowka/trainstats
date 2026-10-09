package com.danilkha.trainstats.features.stats.ui

import com.danilkha.trainstats.features.stats.domain.StatisticsData
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.stats.domain.StatisticsPoint
import com.danilkha.trainstats.features.stats.domain.exerciseStatistics
import com.danilkha.trainstats.features.stats.domain.maximumStatisticsPoints

/** Series zero is the maximum; actual approaches are numbered from one. */
const val MAXIMUM_SERIES = 0

data class StatisticsState(
    val data: StatisticsData = StatisticsData(emptyList(), emptyList()),
    val loading: Boolean = true,
    val failed: Boolean = false,
    val exerciseId: String? = null,
    val searchQuery: String = "",
    val metric: StatisticsMetric = StatisticsMetric.Weight,
    val selectedPoint: StatisticsPoint? = null,
    val visibleSeries: Set<Int> = setOf(MAXIMUM_SERIES),
) {
    val exercise get() = data.exercises.firstOrNull { it.id == exerciseId }
    val filteredExercises get() = data.exercises.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val points: List<StatisticsPoint> by lazy {
        exerciseId?.let { exerciseStatistics(data.workouts, it) }.orEmpty()
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
    val recordPoint by lazy { metricPoints.maxByOrNull { metric.value(it)!! } }
    val recordWorkout get() = data.workouts.firstOrNull { it.id == recordPoint?.workoutId }
}

sealed interface StatisticsEvent {
    data object Retry : StatisticsEvent
    data class Loaded(val data: StatisticsData) : StatisticsEvent
    data object Failed : StatisticsEvent
    data class Search(val query: String) : StatisticsEvent
    data class SelectExercise(val id: String) : StatisticsEvent
    data class SelectMetric(val metric: StatisticsMetric) : StatisticsEvent
    data class SelectPoint(val point: StatisticsPoint) : StatisticsEvent
    data class ToggleSet(val number: Int) : StatisticsEvent
}
