package com.danilkha.trainstats.core.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkha.commoncore.viewmodel.BaseViewModel
import com.danilkha.trainstats.core.utils.findActivity
import com.danilkha.trainstats.di.AppComponent
import com.danilkha.trainstats.di.ViewModelsProvider
import com.danilkha.trainstats.entrypoint.App
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest


@Composable
inline fun<reified T : ViewModel> getCurrentViewModel(crossinline getInstance: (AppComponent) -> T) : T {
    val context = LocalContext.current
    return ViewModelProvider(
        owner = LocalViewModelStoreOwner.current!!,
        factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appComponent = (context.findActivity().application as App).appComponent
                return getInstance(appComponent) as T
            }
        }
    )[T::class.java]
}

@Composable
inline fun<reified T : ViewModel> getViewModel(crossinline getInstance: (ViewModelsProvider) -> T): T {
    val viewModelsProvider = LocalViewModelsProvider.current
    val viewModel = viewModel { getInstance(viewModelsProvider) }
    return viewModel
}

val LocalViewModelsProvider = staticCompositionLocalOf<ViewModelsProvider> { throw IllegalStateException("not initialized") }

@SuppressLint("ComposableNaming")
@Composable
fun <S, E> BaseViewModel<S, E>.collectSingleEvents(onEvent: (event: E) -> Unit){
    LaunchedEffect(key1 = Unit) {
        sideEffects.collectLatest {
            onEvent(it)
        }
    }
}

@Composable
fun <State, SideEffect> BaseViewModel<State, SideEffect>.LaunchCollectEffects(collector: (FlowCollector<SideEffect>)) {
    LaunchedEffect(Unit) {
        sideEffects.collect(collector)
    }
}



