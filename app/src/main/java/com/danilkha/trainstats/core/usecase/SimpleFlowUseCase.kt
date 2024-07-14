package com.danilkha.trainstats.core.usecase

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

abstract class SimpleFlowUseCase<R> {

    operator fun invoke(): Flow<Result<R>> {
        return execute()
            .map {
                Result.success(it)
            }
            .catch {
                Log.w("usecase", "usecase ${this::class.simpleName} fails")
                Log.w("usecase", it)
                emit(Result.failure(it))
            }
    }

    abstract fun execute(): Flow<R>
}