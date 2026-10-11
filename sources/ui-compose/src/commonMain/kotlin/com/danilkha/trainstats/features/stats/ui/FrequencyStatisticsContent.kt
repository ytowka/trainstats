package com.danilkha.trainstats.features.stats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.trainstats.features.stats.domain.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.*
import kotlin.math.ceil

@Composable
internal fun <T> StatisticsSegments(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier.fillMaxWidth().clip(shape).background(Colors.surface)
            .border(1.dp, Colors.outline, shape).selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) Box(Modifier.width(1.dp).height(52.dp).background(Colors.outline))
            Box(
                Modifier.weight(1f).heightIn(min = 52.dp)
                    .background(if (selected == option) Colors.selectionBackground else Color.Transparent)
                    .selectable(selected = selected == option, role = Role.Tab, onClick = { onSelect(option) })
                    .padding(horizontal = 4.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label(option), style = ThemeTypography.body2.copy(fontSize = 14.sp,
                    fontWeight = if (selected == option) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (selected == option) Colors.primary else Colors.text)
            }
        }
    }
}

internal fun LazyListScope.frequencyItems(
    state: StatisticsState,
    chartHeight: Dp,
    onEvent: (StatisticsEvent) -> Unit,
) {
    item(key = "frequency_scope") {
        StatisticsSegments(
            options = FrequencyScope.entries, selected = state.frequencyScope,
            label = { stringResource(if (it == FrequencyScope.Exercise) Res.string.stats_by_exercise else Res.string.stats_all_workouts) },
            modifier = Modifier.testTag("statistics_frequency_scope"),
            onSelect = { onEvent(StatisticsEvent.SelectFrequencyScope(it)) },
        )
    }
    if (state.frequencyScope == FrequencyScope.Exercise) {
        item(key = "frequency_exercise") {
            Card(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large), contentPadding = PaddingValues()) {
                ExerciseDropdown(state, onEvent)
            }
        }
    }
    item(key = "frequency_interval") {
        StatisticsSegments(
            options = FrequencyInterval.entries, selected = state.frequencyInterval,
            label = { frequencyIntervalLabel(it) },
            modifier = Modifier.testTag("statistics_frequency_interval"),
            onSelect = { onEvent(StatisticsEvent.SelectFrequencyInterval(it)) },
        )
    }
    item(key = "frequency_chart") {
        key(state.exerciseId, state.frequencyScope, state.frequencyInterval) {
            FrequencyChart(state, chartHeight, onEvent)
        }
    }
    item(key = "frequency_period_totals") {
        FrequencyTotalsCard(
            title = stringResource(Res.string.stats_selected_period),
            subtitle = frequencyRange(state.selectedFrequencyBucket),
            totals = state.selectedFrequencyBucket.totals,
            modifier = Modifier.testTag("statistics_frequency_period_totals"),
        )
    }
    item(key = "frequency_all_time_totals") {
        FrequencyTotalsCard(
            title = stringResource(Res.string.stats_all_time),
            subtitle = if (state.frequencyScope == FrequencyScope.Exercise) state.exercise?.name else null,
            totals = state.frequency.allTime,
            modifier = Modifier.testTag("statistics_frequency_all_time_totals"),
        )
    }
}

@Composable
private fun FrequencyChart(state: StatisticsState, height: Dp, onEvent: (StatisticsEvent) -> Unit) {
    val buckets = state.frequency.buckets
    val selected = state.selectedFrequencyBucket
    val metric = state.frequencyMetric
    val maxCount = remember(buckets, metric) { buckets.maxOf { it.totals.value(metric) } }
    val tickStep = ceil(maxCount / 4.0).toInt().coerceAtLeast(1)
    val maxY = tickStep * 4
    val textMeasurer = rememberTextMeasurer()
    val grid = Colors.outline
    val textColor = Colors.text
    val primary = Colors.primary
    val selectionBackground = Colors.selectionBackground
    // Position the new interval before its first frame, so older columns never flash.
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = buckets.indexOf(selected))
    val labelStyle = TextStyle(color = textColor.copy(alpha = .65f), fontSize = 10.sp)
    val chartDescription = frequencyChartDescription(metric)
    Card(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(frequencyMetricLabel(metric), color = textColor, style = ThemeTypography.body2)
        Row(Modifier.fillMaxWidth().height(height).testTag("statistics_frequency_chart")
            .semantics { contentDescription = chartDescription }) {
            Canvas(Modifier.width(32.dp).fillMaxHeight()) {
                val top = 24.dp.toPx()
                val bottom = size.height - 42.dp.toPx()
                for (tick in 0..4) {
                    val y = bottom - (bottom - top) * tick / 4
                    val label = textMeasurer.measure((tickStep * tick).toString(), labelStyle)
                    drawText(label, topLeft = Offset((size.width - label.size.width - 6.dp.toPx()).coerceAtLeast(0f), y - label.size.height / 2f))
                }
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
                val columnWidth = (maxWidth / 6).coerceAtLeast(52.dp)
                LazyRow(state = listState, modifier = Modifier.fillMaxSize().testTag("statistics_frequency_bars")) {
                    items(buckets, key = { it.start.toString() }) { bucket ->
                        val count = bucket.totals.value(metric)
                        val active = bucket.start == selected.start
                        val range = frequencyRange(bucket)
                        val description = "$range: ${frequencyMetricLabel(metric)} — $count"
                        val columnLabel = frequencyColumnLabel(bucket, state.frequencyInterval)
                        Canvas(
                            Modifier.width(columnWidth).fillMaxHeight()
                                .semantics { contentDescription = description }
                                .selectable(selected = active, role = Role.RadioButton,
                                    onClick = { onEvent(StatisticsEvent.SelectFrequencyBucket(bucket.start)) }),
                        ) {
                            val top = 24.dp.toPx()
                            val bottom = size.height - 42.dp.toPx()
                            if (active) drawRoundRect(selectionBackground, cornerRadius = CornerRadius(6.dp.toPx()))
                            for (tick in 0..4) {
                                val y = bottom - (bottom - top) * tick / 4
                                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                            }
                            val barHeight = count.toFloat() / maxY * (bottom - top)
                            if (count > 0) drawRoundRect(
                                color = primary.copy(alpha = if (active) 1f else .5f),
                                topLeft = Offset(8.dp.toPx(), bottom - barHeight),
                                size = Size(size.width - 16.dp.toPx(), barHeight),
                                cornerRadius = CornerRadius(3.dp.toPx()),
                            )
                            val valueLabel = textMeasurer.measure(count.toString(), TextStyle(
                                color = if (active) primary else textColor, fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            ))
                            drawText(valueLabel, topLeft = Offset((size.width - valueLabel.size.width) / 2,
                                bottom - barHeight - valueLabel.size.height - 4.dp.toPx()))
                            val dateLabel = textMeasurer.measure(columnLabel, labelStyle)
                            drawText(dateLabel, topLeft = Offset((size.width - dateLabel.size.width) / 2, bottom + 8.dp.toPx()))
                        }
                    }
                }
            }
        }
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FrequencyMetric.entries.forEach { option ->
                val active = metric == option
                val shape = RoundedCornerShape(50)
                Row(
                    Modifier.clip(shape)
                        .background(if (active) selectionBackground else Color.Transparent)
                        .border(1.dp, if (active) primary.copy(alpha = .55f) else grid, shape)
                        .selectable(selected = active, role = Role.RadioButton,
                            onClick = { onEvent(StatisticsEvent.SelectFrequencyMetric(option)) })
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (active) androidx.compose.material.Icon(Icons.Default.Check, null, tint = primary, modifier = Modifier.size(16.dp))
                    Text(frequencyMetricLabel(option), color = if (active) primary else textColor.copy(alpha = .65f),
                        style = ThemeTypography.body2.copy(fontSize = 14.sp))
                }
            }
        }
        if (state.frequency.allTime.workouts == 0) {
            Text(stringResource(if (state.frequencyScope == FrequencyScope.Exercise) Res.string.stats_no_history else Res.string.stats_frequency_no_history),
                style = ThemeTypography.body2, color = textColor)
        }
    }
}

@Composable
private fun FrequencyTotalsCard(title: String, subtitle: String?, totals: FrequencyTotals, modifier: Modifier) {
    Card(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = ThemeTypography.body2, color = Colors.text.copy(alpha = .65f))
        if (subtitle != null) Text(subtitle, style = ThemeTypography.body2, color = Colors.text)
        Row(Modifier.fillMaxWidth()) {
            FrequencyMetric.entries.forEach { metric ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(totals.value(metric).toString(), style = ThemeTypography.body2.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = Colors.primary)
                    Text(frequencyMetricLabel(metric), style = ThemeTypography.body2, color = Colors.text.copy(alpha = .65f))
                }
            }
        }
    }
}

@Composable
internal fun frequencyIntervalLabel(interval: FrequencyInterval): String = stringResource(when (interval) {
    FrequencyInterval.Week -> Res.string.stats_interval_week
    FrequencyInterval.Month -> Res.string.stats_interval_month
    FrequencyInterval.Quarter -> Res.string.stats_interval_quarter
    FrequencyInterval.Year -> Res.string.stats_interval_year
})

@Composable
private fun frequencyMetricLabel(metric: FrequencyMetric): String = stringResource(
    if (metric == FrequencyMetric.Workouts) Res.string.stats_count_workouts else Res.string.stats_count_sets)

@Composable
private fun frequencyChartDescription(metric: FrequencyMetric): String = stringResource(
    Res.string.stats_frequency_chart_description, frequencyMetricLabel(metric))

private fun shortDate(date: LocalDate): String = "${date.day.toString().padStart(2, '0')}.${(date.month.ordinal + 1).toString().padStart(2, '0')}"
private fun frequencyRange(bucket: FrequencyBucket): String {
    val end = bucket.endExclusive.minus(DatePeriod(days = 1))
    return "${shortDate(bucket.start)}.${bucket.start.year} – ${shortDate(end)}.${end.year}"
}

private fun frequencyColumnLabel(bucket: FrequencyBucket, interval: FrequencyInterval): String = when (interval) {
    FrequencyInterval.Week -> "${shortDate(bucket.start)}\n${bucket.start.year}"
    FrequencyInterval.Month -> "${(bucket.start.month.ordinal + 1).toString().padStart(2, '0')}\n${bucket.start.year}"
    FrequencyInterval.Quarter -> "Q${bucket.start.month.ordinal / 3 + 1}\n${bucket.start.year}"
    FrequencyInterval.Year -> bucket.start.year.toString()
}
