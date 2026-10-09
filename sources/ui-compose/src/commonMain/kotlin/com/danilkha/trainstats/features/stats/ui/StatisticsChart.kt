package com.danilkha.trainstats.features.stats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.danilkha.commoncore.utils.format2
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.trainstats.features.stats.domain.StatisticsMetric
import com.danilkha.trainstats.features.stats.domain.StatisticsPoint
import com.danilkha.trainstats.features.stats.domain.statisticsChartRange
import com.danilkha.trainstats.features.stats.domain.maximumStatisticsPoints
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*
import kotlin.math.max
import kotlin.time.Instant

internal fun setColor(number: Int): Color = Color.hsl(((number - 1) * 137.508f + 210f) % 360f, .65f, .48f)

@Composable
internal fun StatisticsChart(
    points: List<StatisticsPoint>,
    metric: StatisticsMetric,
    selectedPoint: StatisticsPoint?,
    onSelect: (StatisticsPoint) -> Unit,
    chartHeight: Dp = 280.dp,
    visibleSeries: Set<Int> = setOf(MAXIMUM_SERIES),
    onToggleSet: (Int) -> Unit,
) {
    val range = remember(points) { statisticsChartRange(points) }
    var zoom by rememberSaveable(range) { mutableStateOf(range.initialZoom) }
    var start by rememberSaveable(range) { mutableStateOf(range.initialStart) }
    val maxZoom = max(20f, range.initialZoom * 20f)
    val latestOnSelect by rememberUpdatedState(onSelect)
    val textMeasurer = rememberTextMeasurer()
    val textColor = Colors.text
    val gridColor = Colors.outline
    val chartDescription = stringResource(Res.string.stats_chart_description)
    val periodDescription = stringResource(Res.string.stats_period_slider)
    val bounds = range.first.toEpochMilliseconds().toDouble() to range.last.toEpochMilliseconds().toDouble()
    val maximumColor = Colors.primary
    val series = remember(points, metric) {
        listOf(MAXIMUM_SERIES to maximumStatisticsPoints(points, metric)) +
            points.groupBy { it.setNumber }.entries.sortedBy { it.key }.map { it.key to it.value }
    }
    val visiblePoints = remember(series, visibleSeries) {
        series.filter { it.first in visibleSeries }.flatMap { it.second }.distinct()
    }
    fun seriesColor(number: Int) = if (number == MAXIMUM_SERIES) maximumColor else setColor(number)
    val maxY = remember(points, metric) { max(points.maxOf { metric.value(it)!! } * 1.1f, 1f) }

    LaunchedEffect(selectedPoint) {
        selectedPoint?.let { point ->
            val fraction = ((point.date.toEpochMilliseconds() - bounds.first) / (bounds.second - bounds.first)).toFloat()
            if (fraction < start || fraction > start + 1f / zoom) {
                start = (fraction - .5f / zoom).coerceIn(0f, 1f - 1f / zoom)
            }
        }
    }

    fun changeZoom(newZoom: Float, anchor: Float = .5f) {
        val oldWidth = 1f / zoom
        val clamped = newZoom.coerceIn(1f, maxZoom)
        start = (start + anchor * (oldWidth - 1f / clamped)).coerceIn(0f, 1f - 1f / clamped)
        zoom = clamped
    }

    Card(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("${metricLabel(metric)} · ${metricUnit(metric)}", color = textColor, style = ThemeTypography.body2)
        Canvas(
            Modifier.fillMaxWidth().height(chartHeight)
                .semantics { contentDescription = chartDescription }
                .pointerInput(points, metric, visibleSeries) {
                    detectTransformGestures { centroid, pan, factor, _ ->
                        val left = 52.dp.toPx()
                        val width = (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)
                        changeZoom(zoom * factor, ((centroid.x - left) / width).coerceIn(0f, 1f))
                        start = (start - pan.x / width / zoom).coerceIn(0f, 1f - 1f / zoom)
                    }
                }
                .pointerInput(points, metric, visibleSeries) {
                    detectTapGestures(
                        onDoubleTap = { zoom = 1f; start = 0f },
                        onTap = { tap ->
                            val left = 52.dp.toPx()
                            val right = size.width - 12.dp.toPx()
                            val top = 12.dp.toPx()
                            val bottom = size.height - 44.dp.toPx()
                            if (tap.x !in left..right || tap.y !in top..bottom) return@detectTapGestures
                            val range = bounds.second - bounds.first
                            val minX = bounds.first + range * start
                            val span = range / zoom
                            val nearest = visiblePoints.mapNotNull { point ->
                                val x = left + ((point.date.toEpochMilliseconds() - minX) / span).toFloat() * (right - left)
                                if (x !in left..right) return@mapNotNull null
                                val y = bottom - metric.value(point)!! / maxY * (bottom - top)
                                point to ((x - tap.x) * (x - tap.x) + (y - tap.y) * (y - tap.y))
                            }.minByOrNull { it.second }
                            if (nearest != null && nearest.second <= 28.dp.toPx().let { it * it }) latestOnSelect(nearest.first)
                        },
                    )
                },
        ) {
            val left = 52.dp.toPx()
            val right = size.width - 12.dp.toPx()
            val top = 12.dp.toPx()
            val bottom = size.height - 44.dp.toPx()
            val minX = bounds.first + (bounds.second - bounds.first) * start
            val span = (bounds.second - bounds.first) / zoom
            fun position(point: StatisticsPoint) = Offset(
                left + ((point.date.toEpochMilliseconds() - minX) / span).toFloat() * (right - left),
                bottom - metric.value(point)!! / maxY * (bottom - top),
            )
            for (tick in 0..4) {
                val y = bottom - (bottom - top) * tick / 4
                drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
                val label = textMeasurer.measure((maxY * tick / 4).format2(), TextStyle(color = textColor, fontSize = 10.sp))
                drawText(label, topLeft = Offset((left - label.size.width - 5.dp.toPx()).coerceAtLeast(0f), y - label.size.height / 2))
            }
            for (tick in 0..2) {
                val x = left + (right - left) * tick / 2
                val dateTime = Instant.fromEpochMilliseconds((minX + span * tick / 2).toLong()).toLocal()
                val labelText = buildString {
                    append(dateTime.date.toString())
                    if (span < 172_800_000.0) {
                        append('\n')
                        append(dateTime.hour.toString().padStart(2, '0'))
                        append(':')
                        append(dateTime.minute.toString().padStart(2, '0'))
                    }
                }
                val label = textMeasurer.measure(labelText, TextStyle(color = textColor, fontSize = 10.sp))
                drawLine(gridColor, Offset(x, top), Offset(x, bottom), strokeWidth = 1.dp.toPx())
                drawText(label, topLeft = Offset((x - label.size.width / 2).coerceIn(0f, (size.width - label.size.width).coerceAtLeast(0f)), bottom + 8.dp.toPx()))
            }
            clipRect(left, top, right, bottom) {
                series.forEach { (number, line) ->
                    if (number !in visibleSeries) return@forEach
                    val color = seriesColor(number)
                    line.zipWithNext().forEach { (a, b) ->
                        drawLine(color, position(a), position(b), strokeWidth = (if (number == MAXIMUM_SERIES) 3.dp else 2.dp).toPx())
                    }
                    line.forEach { point ->
                        val position = position(point)
                        if (position.x in left..right) {
                            drawCircle(color, radius = 4.dp.toPx(), center = position)
                        }
                    }
                }
                selectedPoint?.takeIf { it in visiblePoints }?.let { point ->
                    val selectedSeries = series.first { it.first in visibleSeries && point in it.second }.first
                    drawCircle(seriesColor(selectedSeries), radius = 8.dp.toPx(), center = position(point))
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = position(point))
                }
            }
        }
        if (zoom > 1f) {
            Slider(
                value = start,
                onValueChange = { start = it },
                valueRange = 0f..(1f - 1f / zoom),
                modifier = Modifier.semantics { contentDescription = periodDescription },
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            series.forEach { (number, _) ->
                val enabled = number in visibleSeries
                val color = seriesColor(number)
                val shape = RoundedCornerShape(50)
                Row(
                    modifier = Modifier.clip(shape)
                        .background(if (enabled) color.copy(alpha = .12f) else Color.Transparent)
                        .border(BorderStroke(1.dp, if (enabled) color.copy(alpha = .55f) else gridColor), shape)
                        .toggleable(value = enabled, role = Role.Checkbox, onValueChange = { onToggleSet(number) })
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (enabled) {
                        androidx.compose.material.Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    } else {
                        Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(8.dp).background(color.copy(alpha = .5f), CircleShape))
                        }
                    }
                    Text(
                        if (number == MAXIMUM_SERIES) stringResource(Res.string.stats_maximum) else stringResource(Res.string.stats_set_number, number),
                        color = if (enabled) color else textColor.copy(alpha = .65f),
                        style = ThemeTypography.body2.copy(fontSize = 14.sp, fontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { zoom = 1f; start = 0f }) { Text(stringResource(Res.string.stats_reset_zoom), fontSize = 12.sp) }
            Row {
                IconButton(enabled = zoom > 1f, onClick = { changeZoom(zoom / 1.5f) }) { Text("−", fontSize = 24.sp, color = textColor.copy(alpha = if (zoom > 1f) 1f else .3f)) }
                IconButton(enabled = zoom < maxZoom, onClick = { changeZoom(zoom * 1.5f) }) { Text("+", fontSize = 24.sp, color = textColor) }
            }
        }
    }
}
