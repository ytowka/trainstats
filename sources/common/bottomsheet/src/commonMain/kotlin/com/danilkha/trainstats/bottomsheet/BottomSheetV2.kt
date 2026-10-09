package com.danilkha.trainstats.bottomsheet

import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BottomSheetScreen(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {
    RegisterBottomSheet(state, content)
}


@Composable
internal fun BottomSheetV2(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    closeThreshold: Float = 2 / 3f,
    content: @Composable BoxScope.() -> Unit,
) {
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state.anchoredDraggableState,
        positionalThreshold = { it * closeThreshold },
    )
    val nestedScrollConnection = remember(state, flingBehavior, enabled) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (enabled && available.y < 0 && source == NestedScrollSource.UserInput) {
                    Offset(0f, state.dispatchScrollDelta(available.y))
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (enabled && available.y > 0 && source == NestedScrollSource.UserInput) {
                    Offset(0f, state.dispatchScrollDelta(available.y))
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (enabled && state.anchoredDraggableState.offset > 0) {
                    return Velocity(0f, state.performFling(flingBehavior, available.y))
                }
                return Velocity.Zero
            }


            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (enabled && (available.y != 0f || state.anchoredDraggableState.offset > 0f)) {
                    return Velocity(0f, state.performFling(flingBehavior, available.y))
                }
                return Velocity.Zero
            }
        }
    }
    Box(
        modifier
            .onSizeChanged { state.emitSize(it.height) }
            .nestedScroll(nestedScrollConnection)
            .offset {
                IntOffset(0, state.anchoredDraggableState.requireOffset().roundToInt())
            }
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                enabled = enabled,
                reverseDirection = false,
                orientation = Orientation.Vertical,
                flingBehavior = flingBehavior,
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
    ),
    val onDismiss: () -> Unit = {},
    initialPresentationOrder: Long = nextPresentationOrder(),
    internal val id: Long = initialPresentationOrder,
) {
    var args by mutableStateOf<Map<String, Any>?>(null)


    internal var isShowed by mutableStateOf(anchoredDraggableState.currentValue == BottomSheetExpandState.Expanded)

    internal val isVisible: Boolean get() = isShowed

    internal var presentationOrder by mutableLongStateOf(initialPresentationOrder)
        private set

    private var animationJob: Job? = null
    private var animationId = 0
    private var isTransitioning by mutableStateOf(false)

    init {
        coroutineScope.launch {
            snapshotFlow {
                val collapsedOffset = anchoredDraggableState.anchors
                    .positionOf(BottomSheetExpandState.Collapsed)
                // Distinguish separate closes even if snapshot notifications are coalesced.
                animationId to (isShowed && !isTransitioning &&
                    anchoredDraggableState.settledValue == BottomSheetExpandState.Collapsed &&
                    !collapsedOffset.isNaN() &&
                    abs(anchoredDraggableState.offset - collapsedOffset) < 0.5f)
            }.collect { (_, closed) ->
                if (closed) {
                    isShowed = false
                    args = null
                    onDismiss()
                }
            }
        }
    }

    private fun animateTo(target: BottomSheetExpandState) {
        val requestId = ++animationId
        animationJob?.cancel()
        isTransitioning = true
        animationJob = coroutineScope.launch {
            try {
                snapshotFlow { anchoredDraggableState.anchors }
                    .first { it.hasPositionFor(BottomSheetExpandState.Collapsed) }
                anchoredDraggableState.animateTo(target)
                // A veto can change during the animation. Keep the sheet at its accepted state.
                if (anchoredDraggableState.settledValue != target) {
                    anchoredDraggableState.animateTo(anchoredDraggableState.settledValue)
                }
            } finally {
                if (requestId == animationId) {
                    isTransitioning = false
                }
            }
        }
    }

    internal fun dispatchScrollDelta(delta: Float): Float {
        if (delta != 0f) animationJob?.cancel()
        return anchoredDraggableState.dispatchRawDelta(delta)
    }

    internal suspend fun performFling(flingBehavior: FlingBehavior, velocity: Float): Float {
        var remainingVelocity = velocity
        anchoredDraggableState.anchoredDrag { anchors ->
            val scrollScope = object : ScrollScope {
                override fun scrollBy(pixels: Float): Float {
                    val previousOffset = anchoredDraggableState.requireOffset()
                    val newOffset = (previousOffset + pixels)
                        .coerceIn(anchors.minPosition(), anchors.maxPosition())
                    dragTo(newOffset)
                    return newOffset - previousOffset
                }
            }
            with(flingBehavior) {
                remainingVelocity = scrollScope.performFling(velocity)
            }
        }
        return velocity - remainingVelocity
    }

    internal fun emitSize(height: Int) {
        val newTarget = if (anchoredDraggableState.anchors.hasPositionFor(BottomSheetExpandState.Collapsed)) {
            anchoredDraggableState.targetValue
        } else {
            // Preserve the initial state instead of snapping a new, hidden sheet to Expanded.
            anchoredDraggableState.settledValue
        }
        anchoredDraggableState.updateAnchors(
            DraggableAnchors {
                BottomSheetExpandState.Collapsed at height.coerceAtLeast(1).toFloat()
                BottomSheetExpandState.Expanded at 0f
            },
            newTarget = newTarget,
        )
    }

    // todo: save args through configuration change
    fun show(args: Map<String, Any>? = null) {
        this.args = args
        focusManager.clearFocus()
        presentationOrder = nextPresentationOrder()
        isShowed = true
        animateTo(BottomSheetExpandState.Expanded)
    }

    fun hide() {
        if (isShowed && canHide()) {
            animateTo(BottomSheetExpandState.Collapsed)
        }
    }

    internal fun disposePresentation() {
        ++animationId
        animationJob?.cancel()
        isTransitioning = false
        isShowed = false
        args = null
    }


    // todo: make flow
    fun setResult(result: Map<String, Any>) {
        onResult(result)
    }

    companion object {
        private var lastPresentationOrder = 0L

        private fun nextPresentationOrder(): Long = ++lastPresentationOrder

        fun Saver(
            coroutineScope: CoroutineScope,
            focusManager: FocusManager,
            canHide: () -> Boolean,
            onResult: (Map<String, Any>) -> Unit,
            onDismiss: () -> Unit = {},
        ) =
            androidx.compose.runtime.saveable.Saver<BottomSheetState, Any>(
                save = {
                    listOf(if (it.isShowed) 1L else 0L, it.presentationOrder, it.id)
                },
                restore = restore@{ saved ->
                    // Earlier releases saved only the expand-state enum.
                    val values = when (saved) {
                        is BottomSheetExpandState -> {
                            val order = nextPresentationOrder()
                            listOf(if (saved == BottomSheetExpandState.Expanded) 1L else 0L, order, order)
                        }
                        is List<*> -> saved
                        else -> return@restore null
                    }
                    val visible = values.getOrNull(0) as? Long ?: return@restore null
                    val currentValue = if (visible == 1L) BottomSheetExpandState.Expanded else BottomSheetExpandState.Collapsed
                    val order = values.getOrNull(1) as? Long ?: return@restore null
                    val id = values.getOrNull(2) as? Long ?: order
                    lastPresentationOrder = maxOf(lastPresentationOrder, order, id)
                    BottomSheetState(
                        coroutineScope = coroutineScope,
                        focusManager = focusManager,
                        canHide = canHide,
                        onResult = onResult,
                        onDismiss = onDismiss,
                        initialPresentationOrder = order,
                        id = id,
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
    onDismiss: () -> Unit = {},
): BottomSheetState {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val currentCanHide = rememberUpdatedState(canHide)
    val currentOnResult = rememberUpdatedState(onResult)
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    val canHideCallback = remember { { currentCanHide.value() } }
    val resultCallback = remember { { result: Map<String, Any> -> currentOnResult.value(result) } }
    val dismissCallback = remember { { currentOnDismiss.value() } }
    return rememberSaveable(
        saver = BottomSheetState.Saver(coroutineScope, focusManager, canHideCallback, resultCallback, dismissCallback)
    ) {
        BottomSheetState(
            coroutineScope = coroutineScope,
            focusManager = focusManager,
            canHide = canHideCallback,
            onResult = resultCallback,
            onDismiss = dismissCallback,
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
