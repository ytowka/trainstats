package com.danilkha.commonds.bottomsheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetScreen(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {
    BackHandler(enabled = state.isVisible) {
        state.hide()
    }

    SubcomposeLayout(
        modifier = Modifier.fillMaxSize(),
        measurePolicy = { constraints ->
            if (state.isShowed) {
                if (state.needMeasure) {
                    val subcomposed = subcompose(SubscomposeSlots.Measure) {
                        Box { content() }
                    }.map {
                        it.measure(constraints)
                    }.get(0)

                    state.emitSize(subcomposed.height)
                }

                val placeables = subcompose(SubscomposeSlots.Place) {
                    BottomSheetOverlay(
                        state = state,
                        content = content
                    )
                }.map { it.measure(constraints) }


                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeables.forEach {
                        it.place(0, 0)
                    }
                }
            } else {
                layout(0, 0) {}
            }
        }
    )
}


// todo make fullscreen
@Composable
private fun BottomSheetOverlay(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {
    val targetBackgroundColor =
        if (state.anchoredDraggableState.targetValue == BottomSheetExpandState.Expanded) {
            Color.Black.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        }
    val backgroundColor by animateColorAsState(targetBackgroundColor)

    if (state.isVisible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(color = backgroundColor) }
        ) {
            Spacer(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (state.canHide()) {
                                state.hide()
                            }
                        }
                    )
                    .weight(1f)
                    .fillMaxWidth()
            )
            BottomSheetV2(
                state = state,
                content = content,
            )
        }
    }
}

private enum class SubscomposeSlots { Measure, Place }


@Composable
internal fun BottomSheetV2(
    state: BottomSheetState,
    closeThreshold: Float = 2 / 3f,
    content: @Composable BoxScope.() -> Unit,
) {
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state.anchoredDraggableState,
        positionalThreshold = { it * closeThreshold },
    )
    val scrollFlingScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            state.anchoredDraggableState.dispatchRawDelta(pixels)
            return pixels
        }
    }
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return if (available.y < 0 && source == NestedScrollSource.Drag) {
                Offset(0f, state.anchoredDraggableState.dispatchRawDelta(available.y))
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (available.y > 0 && source == NestedScrollSource.Drag) {
                Offset(0f, state.anchoredDraggableState.dispatchRawDelta(available.y))
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (state.anchoredDraggableState.offset > 0) {
                with(flingBehavior) { scrollFlingScope.performFling(available.y) }
                return available
            }
            return Velocity.Zero
        }


        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return Velocity.Zero
        }
    }
    Box(
        Modifier
            .nestedScroll(nestedScrollConnection)
            .offset {
                IntOffset(0, state.anchoredDraggableState.offset.toInt())
            }
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                reverseDirection = false,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    state = state.anchoredDraggableState,
                    positionalThreshold = { it * closeThreshold },
                ),
            ),
        content = content
    )
}


enum class BottomSheetExpandState {
    Collapsed, Expanded;

    operator fun not(): BottomSheetExpandState {
        return when (this) {
            Collapsed -> Expanded
            Expanded -> Collapsed
        }
    }
}

@Stable
class BottomSheetState(
    val coroutineScope: CoroutineScope,
    val focusManager: FocusManager,
    val canHide: () -> Boolean,
    val onResult: (Map<String, Any>) -> Unit,
    val anchoredDraggableState: AnchoredDraggableState<BottomSheetExpandState> = AnchoredDraggableState(
        initialValue = BottomSheetExpandState.Collapsed,
        confirmValueChange = {
            if (it == BottomSheetExpandState.Collapsed) canHide()
            else true
        },
        anchors = DraggableAnchors {
            BottomSheetExpandState.Expanded at 0f
        }
    )
) {
    var args by mutableStateOf<Map<String, Any>?>(null)


    internal var isShowed by mutableStateOf(anchoredDraggableState.currentValue == BottomSheetExpandState.Expanded)

    internal val needMeasure by derivedStateOf { anchoredDraggableState.anchors.size < 2 }

    internal val isVisible: Boolean by derivedStateOf {
        anchoredDraggableState.settledValue == BottomSheetExpandState.Expanded
                || anchoredDraggableState.targetValue == BottomSheetExpandState.Expanded
    }

    private val targetState = MutableSharedFlow<BottomSheetExpandState>(extraBufferCapacity = 1)

    init {
        coroutineScope.launch {
            targetState
                .onSubscription { emit(anchoredDraggableState.currentValue) }
                .collectLatest {
                    when (it) {
                        BottomSheetExpandState.Collapsed -> {
                            anchoredDraggableState.animateTo(BottomSheetExpandState.Collapsed)
                            isShowed = false
                        }

                        BottomSheetExpandState.Expanded -> {
                            isShowed = true
                            snapshotFlow { anchoredDraggableState.anchors }
                                .first { it.size > 1 }
                            anchoredDraggableState.animateTo(BottomSheetExpandState.Expanded)
                        }
                    }
                }
        }
        coroutineScope.launch {
            snapshotFlow { anchoredDraggableState.settledValue }.collectLatest { isVisible ->
                Napier.d("settledValue = $isVisible")
            }
        }
    }

    internal fun emitSize(height: Int) {
        anchoredDraggableState.updateAnchors(DraggableAnchors {
            BottomSheetExpandState.Collapsed at height.toFloat()
            BottomSheetExpandState.Expanded at 0f
        })
    }

    // todo: save args through configuration change
    fun show(args: Map<String, Any>? = null) {
        this.args = args
        focusManager.clearFocus()
        targetState.tryEmit(BottomSheetExpandState.Expanded)
    }

    fun hide() {
        targetState.tryEmit(BottomSheetExpandState.Collapsed)
    }


    // todo: make flow
    fun setResult(result: Map<String, Any>) {
        onResult(result)
    }

    companion object {
        fun Saver(
            coroutineScope: CoroutineScope,
            focusManager: FocusManager,
            canHide: () -> Boolean,
            onResult: (Map<String, Any>) -> Unit,
        ) =
            androidx.compose.runtime.saveable.Saver<BottomSheetState, BottomSheetExpandState>(
                save = {
                    it.anchoredDraggableState.currentValue
                },
                restore = { currentValue ->
                    BottomSheetState(
                        coroutineScope = coroutineScope,
                        focusManager = focusManager,
                        canHide = canHide,
                        onResult = onResult,
                        anchoredDraggableState = AnchoredDraggableState(
                            initialValue = currentValue,
                            confirmValueChange = {
                                if (it == BottomSheetExpandState.Collapsed) canHide()
                                else true
                            },
                            anchors = DraggableAnchors {
                                BottomSheetExpandState.Expanded at 0f
                            }
                        )
                    )
                },
            )
    }
}

@Composable
fun BottomSheetState.initOnArgs(onArgs: (Map<String, Any>) -> Unit) {
    val args = this.args
    if(args != null) {
        SideEffect {
            onArgs(args)
            this@initOnArgs.args = null
        }
    }
}

@Composable
fun rememberBottomSheetState(
    canHide: () -> Boolean = { true },
    onResult: (Map<String, Any>) -> Unit = {  },
): BottomSheetState {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    return rememberSaveable(saver = BottomSheetState.Saver(coroutineScope, focusManager, canHide, onResult)) {
        BottomSheetState(
            coroutineScope = coroutineScope,
            focusManager = focusManager,
            canHide = canHide,
            onResult = onResult
        )
    }
}


/*
@Preview
@Composable
private fun BottomSheetV2Preview() {
    var expanded by remember { mutableStateOf(BottomSheetExpandState.Collapsed) }
    PreviewContent {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Bottom Sheet ${expanded.name}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BottomSheetScreen(
                content = {
                    BottomSheetContent(
                        title = "Preview",
                        onCloseClicked = { expanded = BottomSheetExpandState.Collapsed }) {
                        Card(
                            modifier = Modifier
                                .background(color = Colors.background)
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Bottom Sheet V2",
                                style = MaterialTheme.typography.h5
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This is a sample bottom sheet using the new AnchoredDraggable API.",
                                style = MaterialTheme.typography.body1
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Features:",
                                style = MaterialTheme.typography.subtitle2
                            )
                            Text(
                                text = "• Migrated from SwipeableState to AnchoredDraggable\n• Simpler API with less boilerplate\n• Same save/restore functionality",
                                style = MaterialTheme.typography.body2
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { expanded = BottomSheetExpandState.Collapsed },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close")
                            }
                        }
                    }
                },
                canHide = { false },
                onStateChange = {
                    expanded = it
                },
                sheetState = expanded,
            )
        }
    }
}*/
