package com.danilkha.uikit.components

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.zIndex
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.PreviewContent
import kotlin.math.roundToInt

@Stable
class DragAndDropState<T>(
    items: List<T>,
    val onItemMove: (from: Int, to: Int) -> Unit
) : DragObserver{

    val offsets = MutableList(items.size) { mutableFloatStateOf(0f) }

    var items by mutableStateOf(items)
        private set

    var draggedItemIndex: Int? = null
    var targetIndex: Int? = null
    private var entries = MutableList(items.size) { DragEntry(0, 0, 0) }

    fun setInternalItems(items: List<T>) {
        for (i in entries.size until items.size) {
            entries.add(DragEntry(0, 0, 0) )
        }
        for (i in offsets.size until items.size) {
            offsets.add(mutableFloatStateOf(0f))
        }

        for (i in items.indices) {
            offsets[i].floatValue = 0f
        }
        this.items = items
    }

    fun emitLayoutCoordinates(index: Int, position: Int, height: Int) {
        entries[index] = DragEntry(index, position, height)
    }


    override fun onDragStart(index: Int) {
        draggedItemIndex = index
        targetIndex = index
    }

    override fun onDrag(dragAmount: Float) {
        val draggedItemIndex = draggedItemIndex
        var targetIndex = targetIndex
        if(draggedItemIndex != null && targetIndex != null) {
            val draggedItemOffset = offsets[draggedItemIndex].floatValue + dragAmount
            var newOffsets = calculateOffsets(draggedItemIndex, targetIndex, entries)
                .toMutableList()
                .apply { set(draggedItemIndex, draggedItemOffset) }

            targetIndex = calculateTargetPos(
                entries = entries,
                dragOffsets = newOffsets,
                draggedItemIndex = draggedItemIndex,
                currentTargetIndex = targetIndex
            )
            this.targetIndex = targetIndex

            newOffsets = calculateOffsets(draggedItemIndex, targetIndex, entries)
                .toMutableList()
                .apply { set(draggedItemIndex, draggedItemOffset) }

            offsets.forEachIndexed { index, state ->
                state.floatValue = newOffsets[index]
            }
        }
    }

    override fun onDragEnd() {
        val localDraggedItemIndex = draggedItemIndex
        val localTargetIndex = targetIndex
        draggedItemIndex = null
        targetIndex = null
        if(localDraggedItemIndex != null && localTargetIndex != null) {
            onItemMove(localDraggedItemIndex, localTargetIndex)
            if(localDraggedItemIndex == localTargetIndex) {
                offsets[localDraggedItemIndex].floatValue = 0f
            }
        }
    }
}

private fun calculateTargetPos(entries: List<DragEntry>, dragOffsets: List<Float>, draggedItemIndex: Int, currentTargetIndex: Int): Int {
    val draggedItemY = entries[draggedItemIndex].position + dragOffsets[draggedItemIndex].fastRoundToInt()
    val draggedItemHeight = entries[draggedItemIndex].height

    val side = currentTargetIndex < draggedItemIndex

    for (i in 0 ..  (currentTargetIndex - if(side) 1 else 0)) {
        if(i == draggedItemIndex) continue
        val y = entries[i].position + dragOffsets[i].fastRoundToInt()
        if(y > draggedItemY) {
            return if(i < draggedItemIndex) {
                i
            } else {
                i - 1
            }
        }
    }

    for(i in (entries.lastIndex - if(side) 1 else 0) downTo (currentTargetIndex)) {
        if(i == draggedItemIndex) continue
        val y = entries[i].position + dragOffsets[i].fastRoundToInt()
        if(y + entries[i].height <= draggedItemY + draggedItemHeight) {
            return if(i > draggedItemIndex) {
                i
            } else {
                i + 1
            }
        }
    }

    return currentTargetIndex
}

fun calculateOffsets(draggedItemIndex: Int, targetIndex: Int, entries: List<DragEntry>): List<Float> {
    val draggedItemHeight = entries[draggedItemIndex].height
    return entries.mapIndexed { index, _ ->
        if(index < draggedItemIndex && index >= targetIndex) {
            draggedItemHeight.toFloat()
        } else if(index > draggedItemIndex && index <= targetIndex){
            -draggedItemHeight.toFloat()
        } else {
            0f
        }
    }
}

@Immutable
data class DragEntry(
    val index: Int, val position: Int, val height: Int
)

fun <T> Modifier.dragAndDropModifier(
    state: DragAndDropState<T>,
    index: Int,
) =
    zIndex(if (index == state.draggedItemIndex) 1f else 0f)
        .onPlaced {
            state.emitLayoutCoordinates(
                index = index,
                position = it.positionInParent().y.roundToInt(),
                height = it.size.height
            )
        }
        .offset {
            IntOffset(0, state.offsets[index].floatValue.roundToInt())
        }


@Composable
@Preview
private fun DragAndDropColumnV3Preview() {
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
                Log.i("debuggg", "DragAndDropColumnV3Preview: $from $to")
            },
            items = items
        ) }
        LaunchedEffect(items) {
            dragAndDropState.setInternalItems(items)
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            dragAndDropState.items.forEachIndexed { index, item ->
                key(item) {
                    var dragged by remember { mutableStateOf(false) }
                    var cords by remember { mutableStateOf(0 to 0) }
                    Text(
                        modifier = Modifier
                            .dragAndDropModifier(dragAndDropState, index)
                            .alpha(if (dragged) 0.5f else 1f)
                            .onPlaced {
                                cords =
                                    it.positionInParent().y.roundToInt() to it.positionInParent().y.roundToInt() + it.size.height
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
                                    onVerticalDrag = { change, dragAmount ->
                                        dragAndDropState.onDrag(
                                            dragAmount
                                        )
                                    }
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