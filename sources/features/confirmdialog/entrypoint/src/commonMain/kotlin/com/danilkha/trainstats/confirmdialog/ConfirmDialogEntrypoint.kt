package com.danilkha.trainstats.confirmdialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.danilkha.commonds.bottomsheet.BottomSheetState

object AlertDialogArgs {

    const val RESULT_BUTTON_ID = "result"

    const val DISMISS_ID = "dismiss"
    const val CONFIRM_ID = "confirm"
    const val CANCEL_ID= "cancel"
}


fun interface ConfirmDialogEntrypoint {
    @Composable
    fun BottomSheetDialog(
        sheetState: BottomSheetState,
        title: String,
        text: String,
    )
}

val LocalConfirmDialogEntrypoint = staticCompositionLocalOf<ConfirmDialogEntrypoint> {
    error("No entrypoint provided")
}

val confirmDialogEntrypoint
    @Composable get() = LocalConfirmDialogEntrypoint.current