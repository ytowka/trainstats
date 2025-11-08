package com.danilkha.uikit.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.PreviewContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> DragAndDropColumnV2(
    modifier: Modifier = Modifier,
    items: List<T>,
    onItemMoved: (fromPos: Int, toPos: Int) -> Unit,
    dragDispatcher: DragDispatcher,
    keyProvider: (Int, T) -> Any,
    content: @Composable (index: Int, item: T) -> Unit,
) {

    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    var targetIndex by remember { mutableStateOf<Int?>(null) }

    var inited by remember { mutableStateOf(false) }
    var lastDraggedItemKey by remember { mutableStateOf<Any?>(null) }
    var targetPositions by remember { mutableStateOf<Map<Any, Int>>(mapOf()) }


    val coroutineScope = rememberCoroutineScope()

    val animatedPositions = remember(items.size) {
        buildMap {
            items.forEachIndexed { index, t ->
                val key = keyProvider(index, t)
                put(key, Animatable((targetPositions[key] ?: 0).toFloat()))
            }
        }
    }

    LaunchedEffect(targetPositions, draggedItemIndex) {
        targetPositions.forEach { (key, value) ->
            launch {
                animatedPositions[key]?.animateTo(value.toFloat(), animationSpec = tween())
            }
        }
        inited = true
    }

    LaunchedEffect(items, dragDispatcher) {
        dragDispatcher.setObserver(object : DragObserver {
            override fun onDragStart(index: Int) {
                targetIndex = index
                lastDraggedItemKey = keyProvider(index, items[index])
                draggedItemIndex = index
            }

            override fun onDrag(dragAmount: Float) {
                dragOffset += dragAmount
            }

            override fun onDragEnd() {
                val finalDragIndex = draggedItemIndex
                val finalTargetIndex = targetIndex
                if(finalDragIndex != null && finalTargetIndex != null){
                    coroutineScope.launch {
                        val key = keyProvider(finalDragIndex, items[finalDragIndex])
                        val movedItemTargetPos = targetPositions[key]!! + dragOffset
                        animatedPositions[key]!!.snapTo(movedItemTargetPos)
                        dragOffset = 0f
                        onItemMoved(finalDragIndex, finalTargetIndex)
                    }
                }
                draggedItemIndex = null
                targetIndex = null
            }
        })
    }

    Box(modifier = modifier) {
        Layout(
            modifier = Modifier,
            content = {
                items.forEachIndexed { index, t ->
                    val key = keyProvider(index, t)
                    key(key) {
                        content(index, t)
                    }
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map {
                it.measure(constraints)
            }
            val height = placeables.sumOf { it.height }

            val draggedItemIndex = draggedItemIndex
            val draggedItemHeight = if(draggedItemIndex != null) {
                placeables[draggedItemIndex].height
            } else 0

            var ys = calculateYs(targetIndex, draggedItemIndex, draggedItemHeight, placeables)

            val localTargetIndex = targetIndex
            if(draggedItemIndex != null && localTargetIndex != null) {
                targetIndex = calculateTargetPos(
                    ys = ys,
                    placeables = placeables,
                    draggedItemIndex = draggedItemIndex,
                    dragOffset = dragOffset,
                    currentTargetIndex = localTargetIndex
                )
            }

            ys = calculateYs(targetIndex, draggedItemIndex, draggedItemHeight, placeables)
            targetPositions = buildMap {
                ys.forEachIndexed { index, y ->
                    val key = keyProvider(index, items[index])
                    put(key, y)
                }
            }

            layout(
                width = constraints.minWidth,
                height = height
            ) {
                placeables.forEachIndexed { index, placeable ->
                    val key = keyProvider(index, items[index])
                    val y = if(inited) animatedPositions[key]!!.value.fastRoundToInt() else ys[index]
                    val zIndex = if(key == lastDraggedItemKey) 1f else 0f
                    val offset = if(index == draggedItemIndex) dragOffset.fastRoundToInt() else 0
                    placeable.place(0, y + offset, zIndex)
                }
            }
        }
    }
}

private fun calculateTargetPos(ys: List<Int>, placeables: List<Placeable>, draggedItemIndex: Int, dragOffset: Float, currentTargetIndex: Int): Int {
    val draggedItemY = ys[draggedItemIndex] + dragOffset.fastRoundToInt()
    val draggedItemHeight = placeables[draggedItemIndex].height

    val side = currentTargetIndex < draggedItemIndex

    for (i in 0 ..  (currentTargetIndex - if(side) 1 else 0)) {
        if(i == draggedItemIndex) continue
        val y = ys[i]
        if(y > draggedItemY) {
            if(i < draggedItemIndex) {
                return i
            } else {
                return i - 1
            }
        }
    }

    for(i in (ys.lastIndex - if(side) 1 else 0) downTo (currentTargetIndex)) {
        if(i == draggedItemIndex) continue
        val y = ys[i]
        if(y + placeables[i].height <= draggedItemY + draggedItemHeight) {
            if(i > draggedItemIndex) {
                return i
            } else {
                return i + 1
            }
        }
    }

    return currentTargetIndex
}

fun calculateYs(localTargetIndex: Int?, draggedItemIndex: Int?, draggedItemHeight: Int, placeables: List<Placeable>): List<Int> {
    var ySum = 0
    return placeables.mapIndexed { index, placeable ->
        val y = ySum
        ySum += placeable.height
        if(draggedItemIndex != null && localTargetIndex != null) {
            if(index < draggedItemIndex && index >= localTargetIndex) {
                y + draggedItemHeight
            } else if(index > draggedItemIndex && index <= localTargetIndex){
                y - draggedItemHeight
            } else {
                y
            }
        } else {
            y
        }
    }
}

@Composable
@Preview
private fun DragAndDropColumnV2Preview() {
    PreviewContent {
        val dragDispatcher = remember { DragDispatcher() }
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DragAndDropColumnV2(
                items = items,
                onItemMoved = { from, to ->
                    items = items.toMutableList().apply { move(from, to) }.toList()
                },
                dragDispatcher = dragDispatcher,
                keyProvider = { index, item -> item }
            ) { index, item ->
                var dragged by remember { mutableStateOf(false) }
                var cords by remember { mutableStateOf(0 to 0) }
                Text(
                    modifier = Modifier
                        //.alpha(if (dragged) 0.5f else 1f)
                        .onPlaced {
                            cords = it.positionInParent().y.roundToInt() to it.positionInParent().y.roundToInt() + it.size.height
                        }
                        .padding(10.dp)
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Colors.primary)
                        .pointerInput(index) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    dragDispatcher.onDragStart(index)
                                    dragged = true
                                },
                                onDragEnd = {
                                    dragDispatcher.onDragEnd()
                                    dragged = false
                                },
                                onVerticalDrag = { change, dragAmount -> dragDispatcher.onDrag(dragAmount) }
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