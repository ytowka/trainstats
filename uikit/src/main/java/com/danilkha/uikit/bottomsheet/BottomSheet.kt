package com.danilkha.uikit.bottomsheet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun BottomSheet(
    closeThreshold: Float = 1 / 4f,
    velocityThreshold: Dp = SwipeableDefaults.VelocityThreshold,
    content: @Composable () -> Unit,
    onHide: () -> Boolean,
    expanded: Boolean = true,
) {
    BoxWithConstraints{
        val bottomSheetState = rememberBottomSheetState(
            confirmStateChange = {
                if (it == BottomSheetExpandState.Collapsed) {
                    onHide()
                } else true
            })

        LaunchedEffect(expanded){
            if(expanded){
                bottomSheetState.open()
            }else{
                bottomSheetState.close()
            }

        }

        val anchors = mapOf(
            constraints.maxHeight.toFloat() to BottomSheetExpandState.Collapsed,
            0f to BottomSheetExpandState.Expanded
        )

        Box(
            Modifier
                .nestedScroll(connection = bottomSheetState.nestedScrollConnection)
                .offset {
                    IntOffset(0, bottomSheetState.offset.value.roundToInt())
                }
                .swipeable(
                    state = bottomSheetState,
                    anchors = anchors,
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(closeThreshold) },
                    resistance = null,
                    velocityThreshold = velocityThreshold,
                )
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal class BottomSheetState(
    val initialState: BottomSheetExpandState = BottomSheetExpandState.Collapsed,
    val confirmStateChange: (BottomSheetExpandState) -> Boolean = { true },
) :  SwipeableState<BottomSheetExpandState>(
    initialValue = initialState,
    animationSpec = tween(),
    confirmStateChange = {
        confirmStateChange(it)
    },
){

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return if (available.y < 0 && source == NestedScrollSource.Drag) {
                performDrag(available.y).let {
                    Offset(0f, it)
                }
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
                performDrag(available.y).let {
                    Offset(0f, it)
                }
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if(offset.value > 0){
                performFling(available.y)
                return available
            }
            return Velocity.Zero
        }


        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return Velocity.Zero
        }
    }

    suspend fun open(){
        snapTo(BottomSheetExpandState.Expanded)
    }

    suspend fun close(){
        animateTo(BottomSheetExpandState.Collapsed)
    }

    companion object {
        fun saver(
            confirmStateChange: (BottomSheetExpandState) -> Boolean = { true },
        ): Saver<BottomSheetState, *> = Saver(
            save = { it.initialState },
            restore = {
                BottomSheetState(
                    initialState = it,
                    confirmStateChange = confirmStateChange,
                )
            }
        )
    }

}

@Composable
internal fun rememberBottomSheetState(
    initialState: BottomSheetExpandState = BottomSheetExpandState.Collapsed,
    confirmStateChange: (BottomSheetExpandState) -> Boolean = { true },
): BottomSheetState = rememberSaveable(saver = BottomSheetState.saver(
    confirmStateChange = confirmStateChange,
)){
    BottomSheetState(
        initialState = initialState,
        confirmStateChange = confirmStateChange,
    )
}

enum class BottomSheetExpandState { Collapsed, Expanded }
