package com.danilkha.trainstats.features.exercises.domain.usecase

import com.danilkha.commoncore.usecase.UseCase
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation

class CreateVariationUseCase(private val repository: ExerciseRepository) : UseCase<CreateVariationUseCase.Params, ExerciseVariation>() {
    data class Params(val exerciseId: String, val name: String)

    override suspend fun execute(params: Params): ExerciseVariation {
        require(params.name.isNotBlank())
        return repository.createVariation(params.exerciseId, normalizeVariationName(params.name))
    }
}
