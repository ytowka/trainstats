package com.danilkha.uikit.bottomsheet

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * BottomSheetController designed for controlling old bottom sheets based on fragment manager
 * system from compose context. The lifecycle of component is bound to parent composition.
 */
open class BottomSheetController(
    val bottomSheetClass: Class<out ComposeContextBottomDialog>,
    val tag: String = bottomSheetClass.name,
    private val fragmentManager: FragmentManager,
    private val onDismiss: () -> Unit,
) {
    var parentCompositionContext: CompositionContext? = null
        set(value) {
            field = value
            val existingFragment = fragmentManager
                .findFragmentByTag(tag) as? ComposeContextBottomDialog
            existingFragment?.parentCompositionContext = value
        }

    init {
        val existingFragment = fragmentManager
            .findFragmentByTag(tag) as? ComposeContextBottomDialog
        existingFragment?.onDismissListener = onDismiss
    }

    fun show(args: Bundle? = null){
        fragmentManager.run {
            val existingFragment = findFragmentByTag(tag)
            if (existingFragment == null) {
                val dialogFragment = bottomSheetClass.newInstance()
                dialogFragment.parentCompositionContext = parentCompositionContext
                dialogFragment.onDismissListener = onDismiss
                if(args != null){
                    dialogFragment.arguments = args
                }
                dialogFragment.showNow(this, tag)
            }
        }
    }

    fun hide(){
        fragmentManager.run {
            val existingFragment = findFragmentByTag(tag) as? ComposeContextBottomDialog
            existingFragment?.dismiss()
        }
    }
}

@Composable
fun rememberBottomSheetController(
    bottomSheetClass: Class<out ComposeContextBottomDialog>,
    viewModelStoreOwner: ViewModelStoreOwner = LocalViewModelStoreOwner.current!!,
    fragmentManager: FragmentManager = LocalFragmentManager.current,
    tag: String = bottomSheetClass.name,
    onDismiss: () -> Unit = {},
): BottomSheetController{

    val controller = remember {
        BottomSheetController(
            bottomSheetClass = bottomSheetClass,
            fragmentManager = fragmentManager,
            tag = tag,
            onDismiss = onDismiss,
        )
    }


    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val compositionContext = rememberCompositionContext()
        LaunchedEffect(key1 = compositionContext){
            controller.parentCompositionContext = compositionContext
        }
    }


    return controller
}