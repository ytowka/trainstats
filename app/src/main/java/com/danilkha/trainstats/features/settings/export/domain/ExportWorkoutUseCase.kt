package com.danilkha.trainstats.features.settings.export.domain

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import com.danilkha.trainstats.core.usecase.SimpleUseCase
import com.danilkha.trainstats.core.utils.format1
import com.danilkha.trainstats.core.utils.format2
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
    private val contentResolver: ContentResolver,
) : SimpleUseCase<String>() {

    override suspend fun execute(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw Exception("supports only android 10 and above")
        }

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

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = contentResolver.insert(collection, contentValues)

            itemUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
        }

        return fileName
    }
}