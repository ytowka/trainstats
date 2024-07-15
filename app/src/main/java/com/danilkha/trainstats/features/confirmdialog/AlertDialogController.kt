package com.danilkha.trainstats.features.confirmdialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentManager
import com.danilkha.uikit.bottomsheet.BottomSheetController
import com.danilkha.uikit.bottomsheet.LocalFragmentManager

class AlertDialogController(
    val fragmentManager: FragmentManager,
    val dialogKey: String,
    val iconRes: Int?,
    val titleRes: Int,
    val textRes: Int,
): BottomSheetController(
    bottomSheetClass = AlertDialogFragment::class.java,
    tag = dialogKey,
    fragmentManager = fragmentManager,
    onDismiss = {}
){
    fun show(){
        show(AlertDialogFragment.buildArgs(dialogKey, iconRes, titleRes, textRes),)
    }
}


@Composable
fun rememberAlertDialog(
    iconRes: Int? = null,
    titleRes: Int,
    textRes: Int,
    dialogKey: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: (() -> Unit)? = null
) : AlertDialogController {
    val fragmentManager = LocalFragmentManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = fragmentManager) {
        fragmentManager.setFragmentResultListener(
            dialogKey,
            lifecycleOwner
        ) { requestKey, result ->
            val id = result.getString(AlertDialogFragment.BUTTON_ID)
            when(id){
                AlertDialogFragment.DISMISS_ID -> onDismiss?.invoke()
                AlertDialogFragment.CONFIRM_ID -> onConfirm()
                AlertDialogFragment.CANCEL_ID -> onCancel()
            }
        }
        onDispose {
            fragmentManager.clearFragmentResultListener(dialogKey)
        }
    }
    val controller = remember {
        AlertDialogController(
            fragmentManager = fragmentManager,
            dialogKey = dialogKey,
            iconRes = iconRes,
            titleRes = titleRes,
            textRes = textRes,
        )
    }

    val compositionContext = rememberCompositionContext()
    LaunchedEffect(key1 = compositionContext){
        controller.parentCompositionContext = compositionContext
    }

    return controller
}