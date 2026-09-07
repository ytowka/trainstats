package com.danilkha.commoncore.usecase

import io.github.aakira.napier.Napier
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
                Napier.w("usecase ${this::class.simpleName} fails with params $params", it)
                emit(Result.failure(it))
            }
    }

    abstract fun execute(params: P): Flow<R>
}