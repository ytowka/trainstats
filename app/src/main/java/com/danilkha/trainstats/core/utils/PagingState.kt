package com.danilkha.trainstats.core.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull

@Immutable
class PagingState<T> (
    val list: List<T>,
    val currentPage: Int = -1,
    val hasNextPage: Boolean = true,
    val loadingPage: Int? = null,
){
    fun aborted(): PagingState<T> = PagingState(
        list,
        currentPage,
        hasNextPage,
        null
    )

    fun updateData(pageList: List<T>, page: Int, hasNextPage: Boolean): PagingState<T> = PagingState(
        list = list.plus(pageList),
        currentPage = page,
        hasNextPage = hasNextPage,
        loadingPage = null
    )

    fun loadNext(): PagingState<T>? {
        if(loadingPage != null || !hasNextPage) return null
        return PagingState(
            list = list,
            currentPage = currentPage,
            hasNextPage = hasNextPage,
            loadingPage = currentPage + 1
        )
    }

    companion object {
        fun <T> initial(): PagingState<T> = PagingState(
            list = emptyList(),
            currentPage = -1,
            hasNextPage = true,
            loadingPage = null,
        )
    }
}

private const val LOADING_TRIGGER_THRESHOLD = 2

@Composable
fun <T> rememberPageableListState(
    state: PagingState<T>,
    nextPageRequest: () -> Unit,
): LazyListState {
    val listState = rememberLazyListState()

    LaunchedEffect(state.hasNextPage){
        if(state.hasNextPage){
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .mapNotNull { it.lastOrNull()?.index }
                .collectLatest {
                    if(it > state.list.size - LOADING_TRIGGER_THRESHOLD){
                       nextPageRequest()
                    }
                }
        }
    }

    return listState
}