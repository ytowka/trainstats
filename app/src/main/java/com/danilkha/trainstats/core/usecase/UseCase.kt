package com.danilkha.trainstats.core.usecase

import android.util.Log

abstract class UseCase<P, R> {

    suspend operator fun invoke(params: P): Result<R>{
        return kotlin.runCatching {
            execute(params)
        }.onFailure {
            Log.w("usecase", "usecase ${this::class.simpleName} failed with params $params")
            Log.w("usecase", it)
        }
    }

    abstract suspend fun execute(params: P): R
}