package com.danilkha.trainstats.bottomsheet

import androidx.compose.runtime.saveable.SaverScope
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class BottomSheetHostStateTest : BehaviorSpec({
    Given("several registered sheets") {
        Then("the order follows show calls, including calls made before registration") {
            runTest {
                val first = createSheet(initialValue = BottomSheetExpandState.Collapsed)
                val second = createSheet(initialValue = BottomSheetExpandState.Collapsed)
                first.show()
                second.show()
                val host = BottomSheetHostState()
                host.register(BottomSheetEntry(second) {})
                host.register(BottomSheetEntry(first) {})
                host.visibleEntries.map { it.state } shouldBe listOf(first, second)

                first.show()
                host.visibleEntries.map { it.state } shouldBe listOf(second, first)
            }
        }

        Then("the top remains modal until closing finishes, then exposes the previous sheet") {
            runTest {
                var firstDismissals = 0
                var secondDismissals = 0
                val first = createSheet(onDismiss = { firstDismissals++ })
                val second = createSheet(onDismiss = { secondDismissals++ })
                first.emitSize(400)
                second.emitSize(200)
                flushSnapshots()
                val host = BottomSheetHostState()
                host.register(BottomSheetEntry(first) {})
                host.register(BottomSheetEntry(second) {})

                host.hideTop()
                host.visibleEntries.map { it.state } shouldBe listOf(first, second)
                flushSnapshots()
                host.visibleEntries.map { it.state } shouldBe listOf(first)
                first.isVisible shouldBe true
                firstDismissals shouldBe 0
                secondDismissals shouldBe 1
            }
        }

        Then("a top-sheet veto never closes the lower sheet") {
            runTest {
                val first = createSheet()
                val second = createSheet(canHide = { false })
                first.emitSize(400)
                second.emitSize(200)
                flushSnapshots()
                val host = BottomSheetHostState()
                host.register(BottomSheetEntry(first) {})
                host.register(BottomSheetEntry(second) {})
                host.hideTop()
                flushSnapshots()
                host.visibleEntries.map { it.state } shouldBe listOf(first, second)
            }
        }

        Then("disposing the owner removes its sheets without dismissal callbacks") {
            runTest {
                var dismissals = 0
                val sheet = createSheet(
                    initialValue = BottomSheetExpandState.Collapsed,
                    onDismiss = { dismissals++ },
                )
                val host = BottomSheetHostState()
                val entry = BottomSheetEntry(sheet) {}
                host.register(entry)
                sheet.show(mapOf("id" to "exercise"))
                flushSnapshots()
                host.unregister(entry)
                flushSnapshots()
                host.visibleEntries shouldBe emptyList()
                sheet.isVisible shouldBe false
                sheet.args shouldBe null
                dismissals shouldBe 0
            }
        }

        Then("restoring sheets in reverse registration order preserves their stack") {
            runTest {
                val first = createSheet()
                val second = createSheet()
                first.show()
                second.show()
                val saver = BottomSheetState.Saver(first.coroutineScope, first.focusManager, { true }, {})
                val saverScope = SaverScope { true }
                val savedFirst = with(saver) { saverScope.save(first) }!!
                val savedSecond = with(saver) { saverScope.save(second) }!!
                val restoredSecond = saver.restore(savedSecond)!!
                val restoredFirst = saver.restore(savedFirst)!!
                restoredFirst.id shouldBe first.id
                restoredSecond.id shouldBe second.id
                val host = BottomSheetHostState()
                host.register(BottomSheetEntry(restoredSecond) {})
                host.register(BottomSheetEntry(restoredFirst) {})
                host.visibleEntries.map { it.state } shouldBe listOf(restoredFirst, restoredSecond)

                restoredFirst.show()
                host.visibleEntries.map { it.state } shouldBe listOf(restoredSecond, restoredFirst)
            }
        }

        Then("the saver accepts visibility saved before the host migration") {
            runTest {
                val sheet = createSheet()
                val saver = BottomSheetState.Saver(sheet.coroutineScope, sheet.focusManager, { true }, {})
                saver.restore(BottomSheetExpandState.Expanded)!!.isVisible shouldBe true
                saver.restore(BottomSheetExpandState.Collapsed)!!.isVisible shouldBe false
            }
        }
    }
})
