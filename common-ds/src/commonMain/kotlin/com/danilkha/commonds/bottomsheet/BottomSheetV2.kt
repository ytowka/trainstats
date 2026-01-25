package com.danilkha.commonds.bottomsheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.PreviewContent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BottomSheetScreen(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {

    SubcomposeLayout(
        modifier = Modifier.fillMaxSize(),
        measurePolicy = { constraints ->
            if (state.isShowed) {
                if(state.needMeasure) {
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
                        it.place(0,0)
                    }
                }
            } else {
                layout(0, 0) {}
            }
        }
    )
}

@Composable
private fun BottomSheetOverlay(
    state: BottomSheetState,
    content: @Composable BoxScope.() -> Unit,
) {
    val targetBackgroundColor = if (state.anchoredDraggableState.targetValue == BottomSheetExpandState.Expanded) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }
    val backgroundColor by animateColorAsState(targetBackgroundColor)

    if(state.isVisible) {
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
                anchoredDraggableState = state.anchoredDraggableState,
                content = content,
            )
        }
    }
}

private enum class SubscomposeSlots { Measure, Place }


@Composable
internal fun BottomSheetV2(
    anchoredDraggableState: AnchoredDraggableState<BottomSheetExpandState>,
    closeThreshold: Float = 2 / 3f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        Modifier
            .offset {
                IntOffset(0, anchoredDraggableState.offset.toInt())
            }
            .anchoredDraggable(
                state = anchoredDraggableState,
                reverseDirection = false,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    state = anchoredDraggableState,
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
    val canHide: () -> Boolean,
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

    internal val needMeasure by derivedStateOf { anchoredDraggableState.anchors.size == 1 }

    internal val isVisible: Boolean by derivedStateOf {
        anchoredDraggableState.settledValue == BottomSheetExpandState.Expanded
                || anchoredDraggableState.targetValue == BottomSheetExpandState.Expanded
    }

    private val targetState = MutableStateFlow(BottomSheetExpandState.Collapsed)

    init {
        coroutineScope.launch {
            targetState.collectLatest {
                when(it) {
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
            snapshotFlow { isVisible }.collectLatest { isVisible ->
                if(!isVisible) {
                    hide()
                }
            }
        }
    }

    internal fun emitSize(height: Int) {
        anchoredDraggableState.updateAnchors(DraggableAnchors {
            BottomSheetExpandState.Collapsed at height.toFloat()
            BottomSheetExpandState.Expanded at 0f
        })
    }

    fun show(args: Map<String, Any>) {
        this.args = args
        targetState.value = BottomSheetExpandState.Expanded
    }

    fun hide() {
        targetState.value = BottomSheetExpandState.Collapsed
    }

    fun popArgs(): Map<String, Any>? {
        val args = args
        this.args = null
        return args
    }

    companion object {
        fun Saver(coroutineScope: CoroutineScope, canHide: () -> Boolean) =
            androidx.compose.runtime.saveable.Saver<BottomSheetState, BottomSheetExpandState>(
                save = {
                    it.anchoredDraggableState.currentValue
                },
                restore = { currentValue ->
                    BottomSheetState(
                        coroutineScope = coroutineScope,
                        canHide = canHide,
                        anchoredDraggableState = AnchoredDraggableState(
                            initialValue = currentValue,
                            confirmValueChange = {
                                if (it == BottomSheetExpandState.Collapsed) canHide()
                                else true
                            }
                        )
                    )
                },
            )
    }
}

@Composable
fun rememberBottomSheetState(canHide: () -> Boolean = { true }): BottomSheetState {
    val coroutineScope = rememberCoroutineScope()
    return rememberSaveable(saver = BottomSheetState.Saver(coroutineScope, canHide)) {
        BottomSheetState(coroutineScope = coroutineScope, canHide = canHide)
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
