package com.danilkha.uikit.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.danilkha.uikit.theme.Colors

@Composable
fun <T> DragAndDropColumn(
    modifier: Modifier = Modifier,
    items: List<T>,
    onItemMoved: (fromPos: Int, toPos: Int) -> Unit,
    dragDispatcher: DragDispatcher,
    keyProvider: (T) -> Any,
    content: @Composable (index: Int, item: T) -> Unit,
) {
    var dragItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragHeight by remember { mutableFloatStateOf(0f) }

    val itemsY = items.map { remember(keyProvider(it)) { mutableFloatStateOf(0f) } }
    val itemsLastTargetOffsets = items.map { remember(keyProvider(it)) { mutableFloatStateOf(0f) } }

    val dragTargetIndex by remember(items) { derivedStateOf {
        if(dragItemIndex != null){
            val positions = itemsY.mapIndexedNotNull { index, state -> state.floatValue.takeIf { index != dragItemIndex } }
            val offsets = itemsLastTargetOffsets.mapIndexedNotNull { index, state -> state.floatValue.takeIf { index != dragItemIndex } }
            calculateTargetIndex(
                positions.zip(offsets){ pos, offset -> pos + offset },
                dragY + dragOffset
            )
        }else null
    } }

    val height = remember { mutableStateListOf(*Array(items.size){0f}) }


    LaunchedEffect(dragDispatcher, items.size) {
        dragDispatcher.setObserver(object : DragObserver {
            override fun onDragStart(index: Int) {
                dragOffset = 0f
                dragItemIndex = index
                dragHeight = height[index]
            }

            override fun onDrag(dragAmount: Float) {
                dragOffset += dragAmount
            }

            override fun onDragEnd() {
                val dragIndex = dragItemIndex
                val targetIndex = dragTargetIndex
                if(dragIndex != null && targetIndex != null){
                    onItemMoved(dragIndex, targetIndex)
                }
                dragItemIndex = null
            }
        })
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, t ->
            val key = keyProvider(t)
            key(key) {
                var y by itemsY[index]
                val animatedOffset = remember(index) {
                    Animatable(0f)
                }
                val lastTargetOffset by itemsLastTargetOffsets[index]
                val targetOffset by remember(index) {
                    derivedStateOf {
                        dragItemIndex?.let{
                            dragHeight * calculateOffsetMultiplier(
                                dragItemIndex = it,
                                dragY = dragY + dragOffset,
                                itemIndex = index,
                                itemY =  y + lastTargetOffset
                            )
                        } ?: 0f
                    }
                }
                LaunchedEffect(targetOffset) {
                    itemsLastTargetOffsets[index].floatValue = targetOffset
                    animatedOffset.animateTo(targetOffset)
                }
                Box(
                    modifier = Modifier
                        .zIndex(if (index == dragItemIndex) 1f else 0f)
                        .onSizeChanged {
                            if (index == dragItemIndex) {
                                dragHeight = it.height.toFloat()
                            }
                            height[index] = it.height.toFloat()
                        }
                        .onPlaced {
                            if (index == dragItemIndex) {
                                dragY = it.positionInParent().y
                            }
                            y = it.positionInParent().y
                        }
                        .offset {
                            if (index == dragItemIndex) {
                                IntOffset(x = 0, dragOffset.toInt())
                            } else {
                                IntOffset(x = 0, y = animatedOffset.value.toInt())
                            }
                        }
                ) {
                    content(index, t)
                }
            }
        }
    }
}

private fun calculateOffsetMultiplier(
    dragItemIndex: Int,
    dragY: Float,
    itemIndex: Int,
    itemY: Float
): Int{
    return if (itemIndex < dragItemIndex) {
        if (dragY < itemY) {
            1
        } else {
            0
        }
    } else if (itemIndex > dragItemIndex) {
        if (dragY > itemY) {
            -1
        } else {
            0
        }
    } else 0
}

private fun calculateTargetIndex(
    itemsPositions: List<Float>,
    dragY: Float,
): Int{
    for((i, pos) in itemsPositions.withIndex()){
        if(dragY < pos) return i
    }
    return itemsPositions.size
}

@Stable
class DragDispatcher(){
    private var dragObserver by mutableStateOf<DragObserver>(object : DragObserver {
        override fun onDragStart(index: Int) {}

        override fun onDrag(dragAmount: Float) {}

        override fun onDragEnd() {}
    })
        private set

    fun setObserver(dragObserver: DragObserver){
        this.dragObserver = dragObserver
    }

    fun onDragStart(index: Int) {
        dragObserver.onDragStart(index)
    }
    fun onDrag(dragAmount: Float) {
        dragObserver.onDrag(dragAmount)
    }
    fun onDragEnd() {
        dragObserver.onDragEnd()
    }
}

interface DragObserver{
    fun onDragStart(index: Int)
    fun onDrag(dragAmount: Float)
    fun onDragEnd();
}