package com.danilkha.commoncore.usecase

import io.github.aakira.napier.Napier

abstract class SimpleUseCase<R> {

    suspend operator fun invoke(): Result<R>{
        return kotlin.runCatching {
            execute()
        }.onFailure {
            Napier.w("usecase ${this::class.simpleName} failed", it)
        }
    }

    abstract suspend fun execute(): R
}