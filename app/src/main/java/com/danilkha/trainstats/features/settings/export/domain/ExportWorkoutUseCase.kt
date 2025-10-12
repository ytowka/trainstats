package com.danilkha.trainstats.features.settings.export.domain

import com.danilkha.trainstats.core.usecase.SimpleUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject

class ExportWorkoutUseCase @Inject constructor(

) : SimpleUseCase<String>() {

    override suspend fun execute(): String {
        delay(1000)
        return "2025.txt"
    }
}