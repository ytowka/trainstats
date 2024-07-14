package com.danilkha.trainstats.core.usecase

import android.util.Log

abstract class SimpleUseCase<R> {

    suspend operator fun invoke(): Result<R>{
        return kotlin.runCatching {
            execute()
        }.onFailure {
            Log.w("usecase", "usecase ${this::class.simpleName} failed")
            Log.w("usecase", it)
        }
    }

    abstract suspend fun execute(): R
}