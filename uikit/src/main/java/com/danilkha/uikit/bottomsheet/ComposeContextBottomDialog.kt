package com.danilkha.uikit.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.danilkha.uikit.R


abstract class ComposeContextBottomDialog : DialogFragment() {

    abstract val content: @Composable () -> Unit

    open val confirmHide: () -> Boolean = {
        dismiss()
        true
    }

    override fun getView(): ComposeView? {
        return super.getView() as? ComposeView
    }


    var parentCompositionContext: CompositionContext? = null
        set(value) {
            field = value
            setContent(view, value)
        }

    var onDismissListener: () -> Unit = {}

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener()
    }


    @get:StyleRes
    open val style: Int = R.style.BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, style)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent(this, parentCompositionContext)
        }
    }

    private fun setContent(view: ComposeView?, parentCompositionContext: CompositionContext?){
        view?.apply {
            if(parentCompositionContext != null){
                setParentCompositionContext(parentCompositionContext)
                setContent {
                    CompositionLocalProvider(LocalFragmentManager provides childFragmentManager) {
                        AndroidViewBottomSheet(
                            confirmHide = confirmHide,
                            content = content
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AndroidViewBottomSheet(
        confirmHide: () -> Boolean,
        expanded: Boolean = true,
        content: @Composable () -> Unit,
    ){
        Column {
            Spacer(modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        confirmHide()
                    }
                )
                .weight(1f)
                .fillMaxWidth()
            )
            BottomSheet(
                content = content,
                onHide = confirmHide,
                expanded = expanded
            )
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }
}

val LocalFragmentManager = staticCompositionLocalOf<FragmentManager> { throw RuntimeException("fragment manager not provided") }