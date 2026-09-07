package com.danilkha.commoncore.usecase

import io.github.aakira.napier.Napier

abstract class UseCase<P, R> {

    suspend operator fun invoke(params: P): Result<R>{
        return kotlin.runCatching {
            execute(params)
        }.onFailure {
            Napier.w("usecase ${this::class.simpleName} failed with params $params", it)
        }
    }

    abstract suspend fun execute(params: P): R
}