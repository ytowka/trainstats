package com.danilkha.trainstats.bottomsheet

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(ExperimentalCoroutinesApi::class)
class BottomSheetStateTest : BehaviorSpec({
    Given("a sheet with scrollable content") {
        When("a nested fling reaches the collapsed anchor") {
            Then("it settles, removes the overlay, and dismisses once") {
                runTest {
                    var dismissals = 0
                    val sheet = createSheet(onDismiss = { dismissals++ })
                    sheet.emitSize(300)
                    flushSnapshots()

                    sheet.dispatchScrollDelta(200f)
                    var consumed = 0f
                    val fling = object : FlingBehavior {
                        override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                            consumed = scrollBy(500f)
                            return 25f
                        }
                    }
                    sheet.performFling(fling, 100f) shouldBe 75f
                    consumed shouldBe 100f
                    sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Collapsed
                    flushSnapshots()
                    sheet.isVisible shouldBe false
                    dismissals shouldBe 1

                    flushSnapshots()
                    dismissals shouldBe 1
                    sheet.show()
                    flushSnapshots()
                    sheet.isVisible shouldBe true
                    sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Expanded
                }
            }
        }

        When("a short drag snaps back to expanded") {
            Then("it stays visible without notifying dismissal") {
                runTest {
                    var dismissals = 0
                    val sheet = createSheet(onDismiss = { dismissals++ })
                    sheet.emitSize(300)
                    flushSnapshots()
                    sheet.dispatchScrollDelta(60f)
                    sheet.performFling(moveBy(-60f), 0f)
                    flushSnapshots()
                    sheet.isVisible shouldBe true
                    sheet.anchoredDraggableState.requireOffset() shouldBe 0f
                    dismissals shouldBe 0
                }
            }
        }

        When("the sheet height changes between openings") {
            Then("the next close reaches the new bottom edge") {
                runTest {
                    val sheet = createSheet()
                    sheet.emitSize(200)
                    flushSnapshots()
                    sheet.hide()
                    flushSnapshots()
                    sheet.isVisible shouldBe false

                    sheet.show()
                    sheet.emitSize(500)
                    flushSnapshots()
                    sheet.hide()
                    flushSnapshots()
                    sheet.anchoredDraggableState.requireOffset() shouldBe 500f
                    sheet.isVisible shouldBe false
                }
            }
        }

        When("the sheet height changes during the closing animation") {
            Then("the animation finishes at the updated anchor") {
                runTest {
                    lateinit var sheet: BottomSheetState
                    val clock = TestFrameClock { sheet.emitSize(500) }
                    sheet = createSheet(frameClock = clock)
                    sheet.emitSize(200)
                    flushSnapshots()
                    sheet.hide()
                    flushSnapshots()
                    sheet.anchoredDraggableState.requireOffset() shouldBe 500f
                    sheet.isVisible shouldBe false
                }
            }
        }

        When("show interrupts a closing animation") {
            Then("the latest request wins without notifying dismissal") {
                runTest {
                    lateinit var sheet: BottomSheetState
                    var dismissals = 0
                    val clock = TestFrameClock { sheet.show() }
                    sheet = createSheet(frameClock = clock, onDismiss = { dismissals++ })
                    sheet.emitSize(300)
                    flushSnapshots()
                    sheet.hide()
                    flushSnapshots()
                    sheet.isVisible shouldBe true
                    sheet.anchoredDraggableState.requireOffset() shouldBe 0f
                    sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Expanded
                    dismissals shouldBe 0
                }
            }
        }
    }

    Given("a sheet that cannot be dismissed") {
        Then("hide keeps it expanded and does not notify dismissal") {
            runTest {
                var dismissals = 0
                val sheet = createSheet(canHide = { false }, onDismiss = { dismissals++ })
                sheet.emitSize(300)
                flushSnapshots()
                sheet.hide()
                flushSnapshots()
                sheet.isVisible shouldBe true
                sheet.anchoredDraggableState.requireOffset() shouldBe 0f
                sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Expanded
                dismissals shouldBe 0
            }
        }

        Then("a veto introduced during closing restores the expanded position") {
            runTest {
                var allowHide = true
                var dismissals = 0
                val sheet = createSheet(
                    canHide = { allowHide },
                    onDismiss = { dismissals++ },
                    frameClock = TestFrameClock { allowHide = false },
                )
                sheet.emitSize(300)
                flushSnapshots()
                sheet.hide()
                flushSnapshots()
                sheet.isVisible shouldBe true
                sheet.anchoredDraggableState.requireOffset() shouldBe 0f
                sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Expanded
                dismissals shouldBe 0
            }
        }
    }

    Given("autofocus waiting for the sheet to open") {
        Then("it waits for measurement and animation before resizing for the keyboard") {
            runTest {
                var frames = 0
                val clock = object : MonotonicFrameClock {
                    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                        yield()
                        return onFrame(++frames * 16_000_000L)
                    }
                }
                val sheet = createSheet(initialValue = BottomSheetExpandState.Collapsed, frameClock = clock)
                var focusRequested = false
                backgroundScope.launch {
                    sheet.awaitExpanded()
                    focusRequested = true
                    sheet.emitSize(600)
                }
                sheet.show()
                flushSnapshots()
                focusRequested shouldBe false
                sheet.emitSize(1000)
                flushSnapshots()
                focusRequested shouldBe true
                // A 300 ms tween takes 20 frames including its initial frame.
                frames shouldBe 20
                sheet.anchoredDraggableState.requireOffset() shouldBe 0f
            }
        }

        Then("an already expanded sheet can focus immediately") {
            runTest {
                val sheet = createSheet()
                sheet.emitSize(300)
                sheet.awaitExpanded()
                sheet.anchoredDraggableState.requireOffset() shouldBe 0f
            }
        }
    }

    Given("a new sheet before its first measurement") {
        Then("show is retained until anchors are available") {
            runTest {
                val sheet = createSheet(initialValue = BottomSheetExpandState.Collapsed)
                sheet.show()
                flushSnapshots()
                sheet.isVisible shouldBe true
                sheet.emitSize(300)
                flushSnapshots()
                sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Expanded
                sheet.anchoredDraggableState.requireOffset() shouldBe 0f
            }
        }

        Then("show followed immediately by hide ends hidden") {
            runTest {
                var dismissals = 0
                val sheet = createSheet(
                    initialValue = BottomSheetExpandState.Collapsed,
                    onDismiss = { dismissals++ },
                )
                sheet.show()
                sheet.hide()
                sheet.emitSize(300)
                flushSnapshots()
                sheet.isVisible shouldBe false
                dismissals shouldBe 1
            }
        }

        Then("the first measurement preserves its collapsed state") {
            runTest {
                val sheet = createSheet(initialValue = BottomSheetExpandState.Collapsed)
                sheet.emitSize(300)
                sheet.anchoredDraggableState.settledValue shouldBe BottomSheetExpandState.Collapsed
                sheet.anchoredDraggableState.requireOffset() shouldBe 300f
                sheet.isVisible shouldBe false
            }
        }
    }
})

internal fun TestScope.createSheet(
    initialValue: BottomSheetExpandState = BottomSheetExpandState.Expanded,
    canHide: () -> Boolean = { true },
    onDismiss: () -> Unit = {},
    frameClock: MonotonicFrameClock = TestFrameClock(),
): BottomSheetState = BottomSheetState(
    coroutineScope = CoroutineScope(backgroundScope.coroutineContext + frameClock),
    focusManager = object : FocusManager {
        override fun clearFocus(force: Boolean) = Unit
        override fun moveFocus(focusDirection: FocusDirection): Boolean = false
    },
    canHide = canHide,
    onResult = {},
    onDismiss = onDismiss,
    anchoredDraggableState = AnchoredDraggableState(
        initialValue = initialValue,
        confirmValueChange = { it != BottomSheetExpandState.Collapsed || canHide() },
        anchors = DraggableAnchors { BottomSheetExpandState.Expanded at 0f },
    ),
)

private fun moveBy(delta: Float): FlingBehavior = object : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        scrollBy(delta)
        return 0f
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun TestScope.flushSnapshots() {
    runCurrent()
    Snapshot.sendApplyNotifications()
    runCurrent()
    Snapshot.sendApplyNotifications()
    runCurrent()
}

private class TestFrameClock(
    private var onFirstFrame: (() -> Unit)? = null,
) : MonotonicFrameClock {
    private var timeNanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
        yield()
        val callback = onFirstFrame
        onFirstFrame = null
        callback?.invoke()
        timeNanos += 16_000_000L
        return onFrame(timeNanos)
    }
}
