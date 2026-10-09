package com.danilkha.trainstats.bottomsheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics

internal val LocalBottomSheetHost = staticCompositionLocalOf<BottomSheetHostState?> { null }

/** Renders registered sheets over [content] in the same window. Place above screen insets. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val host = remember { BottomSheetHostState() }
    val entries = host.visibleEntries
    val top = entries.lastOrNull()
    val focusManager = LocalFocusManager.current
    val scrimColor by animateColorAsState(
        if (entries.size > 1 || top?.state?.anchoredDraggableState?.targetValue == BottomSheetExpandState.Expanded) {
            Color.Black.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        }
    )

    // Run before content autofocus effects, including when a covered sheet becomes top again.
    DisposableEffect(top?.state) {
        if (top != null) focusManager.clearFocus(force = true)
        onDispose {}
    }

    CompositionLocalProvider(LocalBottomSheetHost provides host) {
        Box(modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().modalFocus(enabled = top == null)) {
                content()
            }

            entries.forEach { entry ->
                key(entry.state.id) {
                    val isTop = entry === top
                    Box(Modifier.fillMaxSize().modalFocus(enabled = isTop)) {
                        if (isTop) {
                            BottomSheetScrim(entry.state, scrimColor)
                        }
                        BottomSheetV2(
                            state = entry.state,
                            enabled = isTop,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .safeDrawingPadding()
                                .imePadding(),
                            content = entry.content,
                        )
                    }
                }
            }
        }

        // Registered after navigation and sheet content so Back always targets the top sheet.
        key(top?.state) {
            BackHandler(enabled = top != null) {
                host.hideTop()
            }
        }
    }
}

private fun Modifier.modalFocus(enabled: Boolean): Modifier =
    focusProperties {
        onEnter = { if (!enabled) cancelFocusChange() }
    }.focusGroup().then(if (enabled) Modifier else Modifier.clearAndSetSemantics {})

@Composable
private fun BoxScope.BottomSheetScrim(state: BottomSheetState, color: Color) {
    Box(
        Modifier
            .matchParentSize()
            .drawBehind { drawRect(color) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = state::hide,
            )
    )
}

/** Registers at the caller's lifetime while rendering with its locals at the root host. */
@Composable
internal fun RegisterBottomSheet(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {
    val host = checkNotNull(LocalBottomSheetHost.current) {
        "BottomSheetScreen requires a BottomSheetHost above it."
    }
    val currentContent = rememberUpdatedState(content)
    val currentLocals = rememberUpdatedState(currentCompositionLocalContext)
    val entry = remember(host, state) {
        BottomSheetEntry(state) {
            val scope = this
            CompositionLocalProvider(currentLocals.value) {
                currentContent.value(scope)
            }
        }
    }
    DisposableEffect(host, entry) {
        host.register(entry)
        onDispose { host.unregister(entry) }
    }
}

internal class BottomSheetEntry(
    val state: BottomSheetState,
    val content: @Composable BoxScope.() -> Unit,
)

@Stable
internal class BottomSheetHostState {
    private val registeredEntries = mutableStateListOf<BottomSheetEntry>()

    val visibleEntries by derivedStateOf {
        registeredEntries.filter { it.state.isVisible }.sortedBy { it.state.presentationOrder }
    }

    fun register(entry: BottomSheetEntry) {
        check(registeredEntries.none { it.state === entry.state }) {
            "A BottomSheetState can only be registered once in a host."
        }
        registeredEntries.add(entry)
    }

    fun unregister(entry: BottomSheetEntry) {
        if (registeredEntries.remove(entry)) entry.state.disposePresentation()
    }

    fun hideTop() {
        visibleEntries.lastOrNull()?.state?.hide()
    }
}
