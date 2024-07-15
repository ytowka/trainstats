package com.danilkha.trainstats.features.confirmdialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.danilkha.uikit.bottomsheet.ComposeContextBottomDialog
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography


class AlertDialogFragment : ComposeContextBottomDialog(){


    private val imageRes by lazy {
        arguments?.getLong(IMAGE_RES_KEY)?.takeIf {
            it != NO_IMAGE_VALUE
        }?.toInt()
    }

    private val key by lazy {
        arguments?.getString(DIALOG_KEY) ?: ""
    }

    private val titleRes by lazy {
        arguments?.getInt(TITLE_KEY)
    }

    private val textRes by lazy {
        arguments?.getInt(TEXT_KEY)
    }

    private var dismissedButton = false

    override fun onDismiss(dialog: DialogInterface) {
        if(!dismissedButton){
            setFragmentResult(key, bundleOf(BUTTON_ID to DISMISS_ID))
        }
        super.onDismiss(dialog)
    }

    override val content: @Composable () -> Unit = {
        val image = imageRes?.let { painterResource(id = it) }
        val title = titleRes?.let { stringResource(id = it) }.toString()
        val text = textRes?.let { stringResource(id = it) }.toString()

        AbstractAlertBottomSheetDialog(
            content = {
                Spacer(modifier = Modifier.size(15.dp))
                AlertBottomSheetText(
                    title = title,
                    text = text
                )
            },
            buttons = {
                GenericButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                    ,
                    onClick = {
                        setFragmentResult(key, bundleOf(BUTTON_ID to CONFIRM_ID))
                        dismissedButton = true
                        dismiss()
                    },
                    color = Colors.error
                ) {
                    Text(
                        text = stringResource(id = com.danilkha.trainstats.R.string.delete),
                        color = Colors.surface,
                        style = ThemeTypography.body1
                    )
                }

                GenericButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                    ,
                    onClick = {
                        setFragmentResult(key, bundleOf(BUTTON_ID to CANCEL_ID))
                        dismissedButton = true
                        dismiss()
                    },
                    color = Colors.surface
                ) {
                    Text(
                        text = stringResource(id = com.danilkha.trainstats.R.string.cancel),
                        color = Colors.text,
                        style = ThemeTypography.body1
                    )
                }
            }
        )
    }


    companion object{
        private const val DIALOG_KEY = "dialog_key"
        private const val TITLE_KEY = "title"
        private const val TEXT_KEY = "text"
        private const val IMAGE_RES_KEY = "image"

        private const val NO_IMAGE_VALUE = Long.MAX_VALUE

        const val BUTTON_ID = "button_id"

        const val DISMISS_ID = "dismiss"
        const val CONFIRM_ID = "confirm"
        const val CANCEL_ID= "cancel"

        fun buildArgs(
            key: String,
            iconRes: Int?,
            title: Int,
            text: Int,
        ): Bundle{
            return Bundle().apply {
                putInt(TITLE_KEY, title)
                putString(DIALOG_KEY, key)
                putInt(TEXT_KEY, text)
                putLong(IMAGE_RES_KEY, iconRes?.toLong() ?: NO_IMAGE_VALUE)

            }
        }
    }
}




