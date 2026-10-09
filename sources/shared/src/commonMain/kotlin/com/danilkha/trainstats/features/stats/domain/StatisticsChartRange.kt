package com.danilkha.trainstats.features.stats.domain

import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

data class StatisticsChartRange(
    val first: Instant,
    val last: Instant,
    val initialFirst: Instant,
) {
    val initialZoom: Float get() = ((last - first) / (last - initialFirst)).toFloat()
    val initialStart: Float get() = 1f - 1f / initialZoom
}

/** Show six calendar months ending at the newest workout, including older exercise histories. */
fun statisticsChartRange(points: List<StatisticsPoint>): StatisticsChartRange {
    val last = points.maxOf { it.date } + 12.hours
    val initialFirst = last.minus(DateTimePeriod(months = 6), TimeZone.currentSystemDefault())
    return StatisticsChartRange(
        first = minOf(points.minOf { it.date } - 12.hours, initialFirst),
        last = last,
        initialFirst = initialFirst,
    )
}
