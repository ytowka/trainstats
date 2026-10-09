package com.danilkha.trainstats.features.stats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
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
    hiddenSets: Set<Int> = emptySet(),
    onToggleSet: (Int) -> Unit,
) {
    val range = remember(points) { statisticsChartRange(points) }
    var zoom by rememberSaveable(range) { mutableStateOf(range.initialZoom) }
    var start by rememberSaveable(range) { mutableStateOf(range.initialStart) }
    val maxZoom = max(20f, range.initialZoom * 20f)
    var showInfo by remember { mutableStateOf(false) }
    val latestOnSelect by rememberUpdatedState(onSelect)
    val textMeasurer = rememberTextMeasurer()
    val textColor = Colors.text
    val gridColor = Colors.outline
    val chartDescription = stringResource(Res.string.stats_chart_description)
    val periodDescription = stringResource(Res.string.stats_period_slider)
    val bounds = range.first.toEpochMilliseconds().toDouble() to range.last.toEpochMilliseconds().toDouble()
    val visiblePoints = remember(points, hiddenSets) { points.filter { it.setNumber !in hiddenSets } }
    val series = remember(points) { points.groupBy { it.setNumber }.entries.sortedBy { it.key } }
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${metricLabel(metric)} · ${metricUnit(metric)}", Modifier.weight(1f), color = textColor, style = ThemeTypography.body2)
            Box {
                IconButton(onClick = { showInfo = true }, modifier = Modifier.size(32.dp)) {
                    androidx.compose.material.Icon(Icons.Outlined.Info, stringResource(Res.string.stats_chart_info), tint = textColor.copy(alpha = .6f), modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showInfo, onDismissRequest = { showInfo = false }) {
                    Column(Modifier.width(260.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(when (metric) {
                            StatisticsMetric.Weight -> Res.string.stats_weight_hint
                            StatisticsMetric.Repetitions -> Res.string.stats_repetitions_hint
                            StatisticsMetric.Volume -> Res.string.stats_volume_hint
                        }), style = ThemeTypography.body2)
                        Text(stringResource(Res.string.stats_gesture_hint), style = ThemeTypography.body2)
                    }
                }
            }
        }
        Canvas(
            Modifier.fillMaxWidth().height(chartHeight)
                .semantics { contentDescription = chartDescription }
                .pointerInput(points, metric, hiddenSets) {
                    detectTransformGestures { centroid, pan, factor, _ ->
                        val left = 52.dp.toPx()
                        val width = (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)
                        changeZoom(zoom * factor, ((centroid.x - left) / width).coerceIn(0f, 1f))
                        start = (start - pan.x / width / zoom).coerceIn(0f, 1f - 1f / zoom)
                    }
                }
                .pointerInput(points, metric, hiddenSets) {
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
                    if (number in hiddenSets) return@forEach
                    val color = setColor(number)
                    line.zipWithNext().forEach { (a, b) ->
                        drawLine(color, position(a), position(b), strokeWidth = 2.dp.toPx())
                    }
                    line.forEach { point ->
                        val position = position(point)
                        if (position.x in left..right) {
                            drawCircle(color, radius = 4.dp.toPx(), center = position)
                        }
                    }
                }
                selectedPoint?.takeIf { it in visiblePoints }?.let { point ->
                    drawCircle(setColor(point.setNumber), radius = 8.dp.toPx(), center = position(point))
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            series.forEach { (number, _) ->
                val enabled = number !in hiddenSets
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(setColor(number).copy(alpha = if (enabled) .12f else .03f))
                        .toggleable(value = enabled, role = Role.Checkbox, onValueChange = { onToggleSet(number) })
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(10.dp).background(setColor(number).copy(alpha = if (enabled) 1f else .25f), CircleShape))
                    Text(stringResource(Res.string.stats_set_number, number), color = textColor.copy(alpha = if (enabled) .9f else .4f), style = ThemeTypography.body2.copy(fontSize = 14.sp))
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
