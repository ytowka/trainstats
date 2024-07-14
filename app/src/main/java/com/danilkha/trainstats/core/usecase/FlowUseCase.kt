package com.danilkha.trainstats.core.usecase

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

abstract class FlowUseCase<P, R> {

    operator fun invoke(params: P): Flow<Result<R>> {
        return execute(params)
            .map {
                Result.success(it)
            }
            .catch {
                Log.w("usecase", "usecase ${this::class.simpleName} fails with params $params")
                Log.w("usecase", it)
                emit(Result.failure(it))
            }
    }

    abstract fun execute(params: P): Flow<R>
}