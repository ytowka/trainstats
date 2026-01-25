package com.danilkha.commonds.bottomsheet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Button
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*@Composable
fun BottomSheetScreen() {
    Column(
        modifier = Modifier.safeDrawingPadding()
    ) {
        Spacer(modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    confirmHide()
                }
            )
            .weight(1f)
            .fillMaxWidth()
        )
        BottomSheetV2(
            content = content,
            onHide = confirmHide,
            expanded = expanded
        )
    }
}

interface BottomSheetManager {

    open
}*/

@Composable
internal fun BottomSheetV2(
    closeThreshold: Float = 1 / 3f,
    velocityThreshold: Dp = 125.dp,
    content: @Composable BoxScope.() -> Unit,
    onHide: () -> Boolean,
    expanded: Boolean = true,
) {
    BoxWithConstraints{
        val anchors = mapOf(
            constraints.maxHeight.toFloat() to false,
            0f to true
        )

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = BottomSheetExpandState.Collapsed,
                anchors = DraggableAnchors {
                    BottomSheetExpandState.Collapsed at constraints.maxHeight.toFloat()
                    BottomSheetExpandState.Expanded at 0f
                },
                confirmValueChange = { state ->
                    if(state == BottomSheetExpandState.Expanded) true
                    else onHide()
                }
            )
        }

        Box(
            Modifier
                //.nestedScroll(connection = bottomSheetState.nestedScrollConnection)
                .offset {
                    IntOffset(0, anchoredDraggableState.offset.toInt())
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    reverseDirection = false,
                    orientation = Orientation.Vertical,
                ),
            content = content
        )
    }
}

enum class BottomSheetExpandState { Collapsed, Expanded }

@Preview
@Composable
private fun BottomSheetV2Preview() {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.White)
    ) {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(if (expanded) "Hide Bottom Sheet" else "Show Bottom Sheet")
        }

        Spacer(modifier = Modifier.height(16.dp))

        BottomSheetV2(
            closeThreshold = 1 / 3f,
            velocityThreshold = 125.dp,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        onClick = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            },
            onHide = { expanded = false; true },
            expanded = expanded
        )
    }
}