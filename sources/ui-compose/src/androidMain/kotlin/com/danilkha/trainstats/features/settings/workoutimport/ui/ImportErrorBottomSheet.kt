package com.danilkha.trainstats.features.settings.workoutimport.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.theme.Colors
import com.danilkha.trainstats.bottomsheet.BottomSheetScreen
import com.danilkha.trainstats.bottomsheet.BottomSheetState
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.import_error

internal object ImportErrorBottomSheetArgs {
    const val THROWABLE = "throwable"
}

@Composable
internal fun ImportErrorBottomSheet(
    sheetState: BottomSheetState,
) {
    BottomSheetScreen(state = sheetState) {
        val throwable = sheetState.args?.get(ImportErrorBottomSheetArgs.THROWABLE) as? Throwable
        BoxWithConstraints {
            BottomSheetContent(
                modifier = Modifier.heightIn(max = maxHeight * 0.75f),
                title = stringResource(Res.string.import_error),
                onCloseClicked = { sheetState.hide() },
            ) {
                key(throwable) {
                    val details = remember(throwable) {
                        throwable?.let {
                            "Exception: ${it.javaClass.name}\n\n" +
                                "Message: ${it.message.orEmpty()}\n\n" +
                                it.stackTraceToString()
                        }.orEmpty()
                    }
                    SelectionContainer {
                        Text(
                            text = details,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.caption,
                            color = Colors.text,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
