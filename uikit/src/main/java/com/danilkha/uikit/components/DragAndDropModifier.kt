package com.danilkha.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onFirstVisible
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.PreviewContent
import kotlin.math.roundToInt

@Stable
class DragAndDropState(
    val onItemMove: (from: Int, to: Int) -> Unit

) : DragObserver{

    var draggedItemKey by mutableStateOf<Any?>(null)
    val offsets = mutableMapOf<Any, MutableFloatState>()

    private val entries: MutableMap<Any, DragEntry> = mutableMapOf()

    fun emitLayoutCoordinates(key: Any, position: Int, height: Int) {
        entries[key] = DragEntry(key, position, height)
    }

    fun emitKey(any: Any) {

    }


    override fun onDragStart(index: Int) {
        val entry = entries.values.sortedBy { it.position }[index]
        draggedItemKey = entry.key
    }

    override fun onDrag(dragAmount: Float) {
        draggedItemKey?.let {
            offsets[it]?.floatValue += dragAmount
        }
    }

    override fun onDragEnd() {
        draggedItemKey = null
    }
}

@Immutable
data class DragEntry(
    val key: Any, val position: Int, val height: Int
)

fun Modifier.dragAndDropModifier(
    state: DragAndDropState,
    key: Any,
) =
    zIndex(if (key == state.draggedItemKey) 1f else 0f)
        .onSizeChanged {

        }
        .onPlaced {
            state.emitLayoutCoordinates(
                key = key,
                position = it.positionInParent().y.roundToInt(),
                height = it.size.height
            )
        }
        .offset {
            IntOffset(0, state.offsets[key]?.floatValue?.roundToInt() ?: 0)
        }


@Composable
@Preview
private fun DragAndDropColumnV2Preview() {
    PreviewContent {
        //val dragDispatcher = remember { DragDispatcher() }
        var items by remember {
            mutableStateOf(
                listOf(
                    "item 0",
                    "item 1",
                    "item 2\n\nitem 2",
                    "item 3",
                    "item 4",
                )
            )
        }
        val dragAndDropState = remember { DragAndDropState(
            onItemMove = { from, to ->
                items = items.toMutableList().apply { move(from, to) }.toList()
            }
        ) }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            items.forEachIndexed { index, item ->
                key(item) {
                    var dragged by remember { mutableStateOf(false) }
                    var cords by remember { mutableStateOf(0 to 0) }
                    Text(
                        modifier = Modifier
                            .dragAndDropModifier(dragAndDropState, item)
                            .alpha(if (dragged) 0.5f else 1f)
                            .onPlaced {
                                cords = it.positionInParent().y.roundToInt() to it.positionInParent().y.roundToInt() + it.size.height
                            }
                            .padding(10.dp)
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Colors.primary)
                            .pointerInput(index) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        dragAndDropState.onDragStart(index)
                                        dragged = true
                                    },
                                    onDragEnd = {
                                        dragAndDropState.onDragEnd()
                                        dragged = false
                                    },
                                    onVerticalDrag = { change, dragAmount -> dragAndDropState.onDrag(dragAmount) }
                                )
                            }
                            .background(color = Colors.surface)
                            .padding(30.dp)
                        ,
                        text = "$cords $item"
                    )
                }
            }
        }
    }
}