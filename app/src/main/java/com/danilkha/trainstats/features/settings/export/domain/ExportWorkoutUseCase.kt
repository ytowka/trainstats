package com.danilkha.trainstats.features.settings.export.domain

import com.danilkha.trainstats.core.usecase.SimpleUseCase
import com.danilkha.trainstats.core.utils.format1
import com.danilkha.trainstats.core.utils.format2
import com.danilkha.trainstats.features.settings.export.data.FileWriter
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val fileWriter: FileWriter,
) : SimpleUseCase<String>() {

    override suspend fun execute(): String {
        val nameFormatter = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "workout_export_${nameFormatter.format(Date())}.txt"

        withContext(Dispatchers.IO) {
            val content = buildString {
                workoutRepository.getAll().forEach {
                    appendLine(it.dateTime.format("dd.MM.yyyy"))
                    var lastExercise: String? = null
                    it.steps.forEach { step ->
                        val currentExerciseName = step.exerciseData.name
                        if(currentExerciseName != lastExercise) {
                            appendLine()
                            lastExercise = currentExerciseName
                            appendLine(currentExerciseName)
                        }
                        if(step.weight != null) {
                            append(step.weight.value.format2())
                            append("кг x ")
                        }
                        when(step.reps) {
                            is Repetitions.Double -> {
                                append(step.reps.left.format1())
                                append(", ")
                                append(step.reps.right.format1())
                            }
                            is Repetitions.Single -> append(step.reps.reps.format1())
                        }
                        appendLine()
                    }
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
            }

            fileWriter.writeFile(fileName, content)
        }

        return fileName
    }
}