package com.danilkha.commoncore.usecase

import io.github.aakira.napier.Napier
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
                Napier.w("usecase ${this::class.simpleName} fails", it)
                emit(Result.failure(it))
            }
    }

    abstract fun execute(): Flow<R>
}