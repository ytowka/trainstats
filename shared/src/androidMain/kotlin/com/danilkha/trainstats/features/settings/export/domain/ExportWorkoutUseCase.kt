package com.danilkha.trainstats.features.settings.export.domain

import com.danilkha.commoncore.usecase.SimpleUseCase
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.commoncore.utils.format1
import com.danilkha.commoncore.utils.format2
import com.danilkha.trainstats.features.settings.export.data.FileWriter
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.byUnicodePattern
import javax.inject.Inject

class ExportWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val fileWriter: FileWriter,
) : SimpleUseCase<String>() {

    override suspend fun execute(): String {
        val fileName = getExportFileName()

        val dateFormatter = LocalDate.Format {
            byUnicodePattern("dd.MM.yyyy")
        }
        
        withContext(Dispatchers.IO) {
            val content = buildString {
                workoutRepository.getAll().forEach {
                    appendLine(it.dateTime.toLocal().date.format(dateFormatter))
                    var lastExercise: String? = null
                    it.steps.forEach { step ->
                        val currentExerciseName = step.exerciseData.name
                        if(currentExerciseName != lastExercise) {
                            appendLine()
                            lastExercise = currentExerciseName
                            appendLine(currentExerciseName)
                        }
                        if(step.weight != null) {
                            append(step.weight.value.format2().replace(".", ","))
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

    private fun getExportFileName(): String {
        val date = Clock.System.now().toLocal()
        val formattedDateTime = date.format(LocalDateTime.Format{
            byUnicodePattern("dd-MM-yyyy_HH-mm")
        })
        return "workout_export_$formattedDateTime.txt"
    }
}