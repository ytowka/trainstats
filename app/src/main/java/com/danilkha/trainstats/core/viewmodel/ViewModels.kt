package com.danilkha.trainstats.core.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.danilkha.trainstats.core.usecase.UseCase
import com.danilkha.trainstats.core.utils.findActivity
import com.danilkha.trainstats.di.AppComponent
import com.danilkha.trainstats.entrypoint.App
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
inline fun<reified T : ViewModel> activityViewModel(crossinline getInstance: (AppComponent) -> T) : T {
    val context = LocalContext.current
    val activity = context.findActivity()
    return ViewModelProvider(
        owner = activity,
        factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appComponent = (activity.application as App).appComponent
                return getInstance(appComponent) as T
            }
    })[T::class.java]
}


inline fun<reified T : ViewModel> Context.activityViewModel(crossinline getInstance: (AppComponent) -> T) : Lazy<T> = lazy{
    val activity = findActivity()
    ViewModelProvider(
        owner = activity,
        factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appComponent = (activity.application as App).appComponent
                return getInstance(appComponent) as T
            }
        })[T::class.java]
}

inline fun<reified T : ViewModel> Fragment.viewModel(crossinline getInstance: (AppComponent) -> T) : Lazy<T> = lazy{
    ViewModelProvider(
        owner = this,
        factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appComponent = (activity?.application as App).appComponent
                return getInstance(appComponent) as T
            }
        })[T::class.java]
}

@SuppressLint("ComposableNaming")
@Composable
fun <S, E> BaseViewModel<S, E>.collectSingleEvents(onEvent: (event: E) -> Unit){
    LaunchedEffect(key1 = Unit) {
        sideEffects.collectLatest {
            onEvent(it)
        }
    }
}


