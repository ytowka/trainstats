package com.danilkha.trainstats.features.workout.ui.editor

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commonds.theme.Colors
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun WorkoutStopwatch(lastEdited: Instant) {
    var now by remember(lastEdited) { mutableStateOf(Clock.System.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lastEdited, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = Clock.System.now()
                delay(1_000)
            }
        }
    }

    if (lastEdited.toLocal().date == now.toLocal().date) {
        val elapsedSeconds = (now - lastEdited).inWholeSeconds.coerceAtLeast(0)
        val elapsed = listOf(
            elapsedSeconds / 3_600,
            elapsedSeconds / 60 % 60,
            elapsedSeconds % 60
        ).joinToString(":") { it.toString().padStart(2, '0') }

        Text(
            text = elapsed,
            style = MaterialTheme.typography.caption,
            fontFamily = FontFamily.Monospace,
            color = Colors.text.copy(alpha = 0.6f)
        )
    }
}
