package com.danilkha.trainstats

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danilkha.trainstats.bottomsheet.BottomSheetHost
import com.danilkha.trainstats.bottomsheet.BottomSheetExpandState
import com.danilkha.trainstats.bottomsheet.BottomSheetScreen
import com.danilkha.trainstats.bottomsheet.BottomSheetState
import com.danilkha.trainstats.bottomsheet.rememberBottomSheetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private val LocalSheetTestValue = staticCompositionLocalOf { "root" }

@RunWith(AndroidJUnit4::class)
class BottomSheetHostTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun autofocusWaitsUntilFullyOpenOnFirstAndRepeatedPresentation() {
        lateinit var state: BottomSheetState
        val focusOffsets = mutableListOf<Float>()
        rule.setContent {
            BottomSheetHost {
                state = rememberBottomSheetState()
                BottomSheetScreen(state) {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(state) {
                        state.awaitExpanded()
                        focusOffsets.add(state.anchoredDraggableState.requireOffset())
                        focusRequester.requestFocus()
                    }
                    Box(Modifier.fillMaxSize().background(Color.White).testTag("sheet")) {
                        BasicTextField("search", {}, Modifier.focusRequester(focusRequester).testTag("search"))
                    }
                }
            }
        }
        repeat(2) { presentation ->
            rule.runOnIdle { state.show() }
            rule.onNodeWithTag("search").assertIsFocused()
            rule.runOnIdle {
                assertEquals(presentation + 1, focusOffsets.size)
                assertEquals(0f, focusOffsets.last(), 0.5f)
            }
            rule.runOnIdle { state.hide() }
            rule.onNodeWithTag("sheet").assertDoesNotExist()
        }
    }

    @Test
    fun nestedDeclarationRendersAtRootWithCallerLocals() {
        lateinit var state: BottomSheetState
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        var renderedOwner: ViewModelStoreOwner? = null
        var instances = 0
        rule.setContent {
            BottomSheetHost(Modifier.testTag("host")) {
                state = rememberBottomSheetState()
                Box(Modifier.size(60.dp).testTag("caller")) {
                    CompositionLocalProvider(
                        LocalSheetTestValue provides "caller",
                        LocalViewModelStoreOwner provides owner,
                    ) {
                        BottomSheetScreen(state) {
                            remember { instances++ }
                            renderedOwner = LocalViewModelStoreOwner.current
                            Box(Modifier.fillMaxWidth().height(120.dp).testTag("sheet")) {
                                Text(LocalSheetTestValue.current)
                            }
                        }
                    }
                }
            }
        }
        rule.runOnIdle { state.show() }
        val hostBounds = rule.onNodeWithTag("host").fetchSemanticsNode().boundsInRoot
        val sheetBounds = rule.onNodeWithTag("sheet").fetchSemanticsNode().boundsInRoot
        assertTrue(sheetBounds.width > hostBounds.width * 0.9f)
        assertTrue(sheetBounds.top > 60 * rule.density.density)
        rule.onNodeWithText("caller").assertExists()
        rule.runOnIdle {
            assertTrue(renderedOwner === owner)
            assertEquals(1, instances)
        }
    }

    @Test
    fun coveringSheetPreservesEditorAndBlocksItsFocusAndTouches() {
        lateinit var editor: BottomSheetState
        lateinit var confirmation: BottomSheetState
        val editorFocus = FocusRequester()
        val topFocus = FocusRequester()
        var instances = 0
        var disposals = 0
        var lowerClicks = 0
        var dismissals = 0
        rule.setContent {
            BottomSheetHost(Modifier.testTag("host")) {
                editor = rememberBottomSheetState()
                confirmation = rememberBottomSheetState(onDismiss = { dismissals++ })
                // Declare in reverse order to exercise ordering by show(), not composition.
                BottomSheetScreen(confirmation) {
                    BasicTextField(
                        value = "confirmation",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                            .focusRequester(topFocus).testTag("top-field"),
                    )
                }
                BottomSheetScreen(editor) {
                    remember { instances++ }
                    DisposableEffect(Unit) { onDispose { disposals++ } }
                    var value by remember { mutableStateOf("") }
                    Column(Modifier.fillMaxWidth().height(260.dp).background(Color.White)) {
                        Box(Modifier.fillMaxWidth().height(100.dp).clickable { lowerClicks++ }
                            .testTag("lower-button"))
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier.focusRequester(editorFocus).testTag("editor-field"),
                        )
                    }
                }
            }
        }
        rule.runOnIdle { editor.show() }
        rule.onNodeWithTag("editor-field").performTextInput("saved text")
        val lowerButton = rule.onNodeWithTag("lower-button").fetchSemanticsNode().boundsInRoot.center
        rule.runOnIdle { confirmation.show() }
        rule.runOnIdle {
            assertEquals(1, instances)
            assertEquals(0, disposals)
            assertTrue(topFocus.requestFocus())
            assertFalse(editorFocus.requestFocus())
        }
        rule.runOnIdle {
            assertEquals(BottomSheetExpandState.Expanded, confirmation.anchoredDraggableState.settledValue)
            assertEquals(0f, confirmation.anchoredDraggableState.requireOffset(), 0.5f)
        }
        rule.onNodeWithTag("top-field").assertIsFocused()
        // Tap the exposed lower sheet through the top sheet's scrim.
        rule.onNodeWithTag("host").performTouchInput { click(lowerButton) }
        rule.onNodeWithTag("editor-field").assertTextEquals("saved text")
        rule.runOnIdle {
            assertEquals(0, lowerClicks)
            assertEquals(1, dismissals)
            assertEquals(1, instances)
            assertEquals(0, disposals)
        }
    }

    @Test
    fun backTargetsTopSheetAndRespectsItsVeto() {
        lateinit var lower: BottomSheetState
        lateinit var upper: BottomSheetState
        var allowHide by mutableStateOf(false)
        var lowerDismissals = 0
        var upperDismissals = 0
        var navigationBacks = 0
        rule.setContent {
            BottomSheetHost {
                androidx.activity.compose.BackHandler { navigationBacks++ }
                lower = rememberBottomSheetState(onDismiss = { lowerDismissals++ })
                upper = rememberBottomSheetState(canHide = { allowHide }, onDismiss = { upperDismissals++ })
                BottomSheetScreen(lower) { Box(Modifier.fillMaxWidth().height(260.dp).testTag("lower")) }
                BottomSheetScreen(upper) { Box(Modifier.fillMaxWidth().height(120.dp).testTag("upper")) }
            }
        }
        rule.runOnIdle { lower.show(); upper.show() }
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithTag("upper").assertExists()
        rule.runOnIdle {
            assertEquals(0, lowerDismissals)
            assertEquals(0, upperDismissals)
            assertEquals(0, navigationBacks)
            allowHide = true
        }
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithTag("upper").assertDoesNotExist()
        rule.onNodeWithTag("lower").assertExists()
        rule.runOnIdle {
            assertEquals(0, lowerDismissals)
            assertEquals(1, upperDismissals)
            assertEquals(0, navigationBacks)
        }
    }

    @Test
    fun closingTopSheetBlocksLowerInputUntilAnimationFinishes() {
        lateinit var lower: BottomSheetState
        lateinit var upper: BottomSheetState
        val lowerFocus = FocusRequester()
        rule.setContent {
            BottomSheetHost {
                lower = rememberBottomSheetState()
                upper = rememberBottomSheetState()
                BottomSheetScreen(lower) {
                    BasicTextField("lower", {}, Modifier.fillMaxWidth().height(260.dp)
                        .focusRequester(lowerFocus).testTag("lower-field"))
                }
                BottomSheetScreen(upper) { Box(Modifier.fillMaxWidth().height(120.dp).testTag("upper")) }
            }
        }
        rule.runOnIdle { lower.show(); upper.show() }
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { upper.hide() }
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { assertFalse(lowerFocus.requestFocus()) }
        rule.onNodeWithTag("upper").assertExists()
        rule.mainClock.advanceTimeBy(2_000)
        rule.mainClock.autoAdvance = true
        rule.onNodeWithTag("upper").assertDoesNotExist()
        rule.runOnIdle { assertTrue(lowerFocus.requestFocus()) }
    }

    @Test
    fun leavingOwnerRemovesAllOfItsSheets() {
        lateinit var first: BottomSheetState
        lateinit var second: BottomSheetState
        var showOwner by mutableStateOf(true)
        rule.setContent {
            BottomSheetHost {
                if (showOwner) {
                    first = rememberBottomSheetState()
                    second = rememberBottomSheetState()
                    BottomSheetScreen(first) { Box(Modifier.fillMaxWidth().height(260.dp).testTag("first")) }
                    BottomSheetScreen(second) { Box(Modifier.fillMaxWidth().height(120.dp).testTag("second")) }
                }
            }
        }
        rule.runOnIdle { first.show(); second.show() }
        rule.onNodeWithTag("second").assertExists()
        rule.runOnIdle { showOwner = false }
        rule.onNodeWithTag("first").assertDoesNotExist()
        rule.onNodeWithTag("second").assertDoesNotExist()
    }

    @Test
    fun restorationPreservesStackOrderAndEditorSavedState() {
        lateinit var editor: BottomSheetState
        lateinit var confirmation: BottomSheetState
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            BottomSheetHost {
                confirmation = rememberBottomSheetState()
                editor = rememberBottomSheetState()
                BottomSheetScreen(confirmation) {
                    Box(Modifier.fillMaxWidth().height(120.dp).testTag("confirmation"))
                }
                BottomSheetScreen(editor) {
                    var text by rememberSaveable { mutableStateOf("") }
                    BasicTextField(text, { text = it }, Modifier.fillMaxWidth().height(260.dp).testTag("saved-editor"))
                }
            }
        }
        rule.runOnIdle { editor.show() }
        rule.onNodeWithTag("saved-editor").performTextInput("saved across restore")
        rule.runOnIdle { confirmation.show() }
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("confirmation").assertExists()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithTag("confirmation").assertDoesNotExist()
        rule.onNodeWithTag("saved-editor").assertTextEquals("saved across restore")
    }
}
