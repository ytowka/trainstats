package com.danilkha.trainstats.confirmdialog

val ConfirmDialogEntrypointProvider = LocalConfirmDialogEntrypoint provides
        ConfirmDialogEntrypoint { sheetState, title, text ->
            AlertBottomSheetDialog(sheetState, title, text)
        }
