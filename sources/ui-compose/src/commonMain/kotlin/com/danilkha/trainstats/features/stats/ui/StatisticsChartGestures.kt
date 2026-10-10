package com.danilkha.trainstats.features.stats.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

internal suspend fun PointerInputScope.detectStatisticsChartGestures(
    onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
) = awaitEachGesture {
    // The tap detector also observes this down; movement decides who owns the gesture.
    awaitFirstDown(requireUnconsumed = false)
    var accumulatedPan = Offset.Zero
    var chartOwnsGesture = false
    do {
        val event = awaitPointerEvent()
        if (event.changes.any { it.isConsumed }) break

        val pan = event.calculatePan()
        val multiTouch = event.changes.count { it.pressed } > 1
        if (!chartOwnsGesture) {
            accumulatedPan += pan
            if (multiTouch) {
                chartOwnsGesture = true
            } else if (accumulatedPan.getDistance() > viewConfiguration.touchSlop) {
                // Leave vertical movement unconsumed so the enclosing list can scroll.
                if (abs(accumulatedPan.y) >= abs(accumulatedPan.x)) break
                chartOwnsGesture = true
            }
        }

        if (chartOwnsGesture) {
            val zoom = if (multiTouch) event.calculateZoom() else 1f
            if (pan != Offset.Zero || zoom != 1f) {
                onTransform(event.calculateCentroid(useCurrent = false), pan, zoom)
            }
            event.changes.forEach { if (it.positionChanged()) it.consume() }
        }
    } while (event.changes.any { it.pressed })
}
