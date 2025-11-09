package com.danilkha.uikit.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.PreviewContent
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun <T> DragAndDropColumn(
    modifier: Modifier = Modifier,
    items: List<T>,
    onItemMoved: (fromPos: Int, toPos: Int) -> Unit,
    dragDispatcher: DragDispatcher,
    keyProvider: (Int, T) -> Any,
    content: @Composable (index: Int, item: T) -> Unit,
) {
    var dragItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragHeight by remember { mutableFloatStateOf(0f) }

    val itemsY = items.mapIndexed { index, it ->
        remember(
            keyProvider(
                index,
                it
            )
        ) { mutableFloatStateOf(0f) }
    }
    val itemsLastTargetOffsets = items.mapIndexed { index, it ->
        remember(
            keyProvider(
                index,
                it
            )
        ) { mutableFloatStateOf(0f) }
    }

    val dragTargetIndex by remember(items) {
        derivedStateOf {
            if (dragItemIndex != null) {
                val positions =
                    itemsY.mapIndexedNotNull { index, state -> state.floatValue.takeIf { index != dragItemIndex } }
                val offsets =
                    itemsLastTargetOffsets.mapIndexedNotNull { index, state -> state.floatValue.takeIf { index != dragItemIndex } }
                calculateTargetIndex(
                    positions.zip(offsets) { pos, offset -> pos + offset },
                    dragY + dragOffset
                )
            } else null
        }
    }

    val height = remember(items.size) { mutableStateListOf(*Array(items.size) { 0f }) }

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
                if (dragIndex != null && targetIndex != null) {
                    onItemMoved(dragIndex, targetIndex)
                }
                dragItemIndex = null
            }
        })
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, t ->
            val key = keyProvider(index, t)
            key(key) {
                var y by itemsY[index]
                val animatedOffset = remember(index) {
                    Animatable(0f)
                }
                val lastTargetOffset by itemsLastTargetOffsets[index]
                val targetOffset by remember(index) {
                    derivedStateOf {
                        dragItemIndex?.let {
                            dragHeight * calculateOffsetMultiplier(
                                dragItemIndex = it,
                                dragY = dragY + dragOffset,
                                itemIndex = index,
                                itemY = y + lastTargetOffset
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
): Int {
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

fun <T> MutableList<T>.move(from: Int, to: Int) {
    add(to, removeAt(from))
}

private fun calculateTargetIndex(
    itemsPositions: List<Float>,
    dragY: Float,
): Int {
    for ((i, pos) in itemsPositions.withIndex()) {
        if (dragY < pos) return i
    }
    return itemsPositions.size
}

@Stable
class DragDispatcher() {
    private var dragObserver: DragObserver = object : DragObserver {
        override fun onDragStart(index: Int) {}

        override fun onDrag(dragAmount: Float) {}

        override fun onDragEnd() {}
    }

    fun setObserver(dragObserver: DragObserver) {
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

interface DragObserver {
    fun onDragStart(index: Int)
    fun onDrag(dragAmount: Float)
    fun onDragEnd();
}


var count1 = 0
var count2 = 0

@Composable
@Preview
private fun DragAndDropColumnV2Preview() {
    PreviewContent {
        var y1 by remember { mutableIntStateOf(0) }
        var y2 by remember { mutableIntStateOf(0) }



        val offset = remember { mutableIntStateOf(100) }
        var countText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            repeat(10) {
                offset.intValue += 10
                delay(50)
            }
            countText = "$count1 $count2"
        }

        Box(modifier = Modifier.size(100.dp)){
            Text(
                modifier = Modifier
                    .onSizeChanged {
                        count1++
                    }
                    .onPlaced() {
                        y1 = it.positionInParent().y.roundToInt()
                    }
                    .offset {
                        IntOffset(0, offset.intValue)
                    }
                    .onPlaced {
                        count2++
                        y2 = it.positionInParent().y.roundToInt()
                    }
                ,
                text = countText
            )

            Text(
                text = "$y1\n$y2"
            )
        }
    }
}