package com.danilkha.trainstats.features.stats.ui

import com.danilkha.trainstats.features.stats.domain.StatisticsData
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.stats.domain.StatisticsPoint
import com.danilkha.trainstats.features.stats.domain.exerciseStatistics

data class StatisticsState(
    val data: StatisticsData = StatisticsData(emptyList(), emptyList()),
    val loading: Boolean = true,
    val failed: Boolean = false,
    val exerciseId: String? = null,
    val searchQuery: String = "",
    val metric: StatisticsMetric = StatisticsMetric.Weight,
    val selectedPoint: StatisticsPoint? = null,
    val hiddenSets: Set<Int> = emptySet(),
) {
    val exercise get() = data.exercises.firstOrNull { it.id == exerciseId }
    val filteredExercises get() = data.exercises.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val points: List<StatisticsPoint> by lazy {
        exerciseId?.let { exerciseStatistics(data.workouts, it) }.orEmpty()
    }
    val selectedWorkout get() = data.workouts.firstOrNull { it.id == selectedPoint?.workoutId }
    val visiblePoints get() = points.filter {
        it.setNumber !in hiddenSets && metric.value(it)?.let { value -> value.isFinite() && value >= 0f } == true
    }
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
