package com.danilkha.trainstats.features.settings

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.features.exercises.domain.usecase.CreateVariationUseCase
import com.danilkha.trainstats.features.exercises.data.db.RoomExerciseDatasource
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseRepositoryImpl
import com.danilkha.trainstats.features.settings.export.data.FileWriter
import com.danilkha.trainstats.features.settings.export.domain.ExportWorkoutUseCase
import com.danilkha.trainstats.features.settings.workoutimport.data.FileReader
import com.danilkha.trainstats.features.settings.workoutimport.data.WorkoutParserImpl
import com.danilkha.trainstats.features.settings.workoutimport.domain.ImportWorkoutsUseCase
import com.danilkha.trainstats.features.workout.data.db.RoomWorkoutDatasource
import com.danilkha.trainstats.features.workout.data.db.createTestDb
import com.danilkha.trainstats.features.workout.data.repository.WorkoutRepositoryImpl
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExportImportTest {

    @Test
    fun exportImportTest() = runTest {
        val original = readAsset("workout_export.txt")
        importThenExport(original).trim() shouldBe original.trim()
    }

    @Test
    fun importExportPreservesNovember2025FileExactly() = runTest {
        val original = readAsset("workout_export_16-11-2025_19-02.txt")
        importThenExport(original) shouldBe original
    }

    @Test
    fun importExportPreservesNovember2025FileWithVariationBlocksExactly() = runTest {
        val original = readAsset("workout_export_16-11-2025_19-02.txt")
        val ordinaryBlock = """
            вертикальная тяга
            40кг x 12
            40кг x 12
            40кг x 12
        """.trimIndent()
        val variationBlocks = """
            вертикальная тяга ${'$'} Узкая ручка
            40кг x 12

            вертикальная тяга
            40кг x 12

            вертикальная тяга ${'$'} Широкая ручка
            40кг x 12
        """.trimIndent()
        check(ordinaryBlock in original) { "The fixture must contain the block replaced with variations" }
        val withVariations = original.replaceFirst(ordinaryBlock, variationBlocks)

        importThenExport(withVariations) shouldBe withVariations
    }

    private fun readAsset(name: String): String =
        InstrumentationRegistry.getInstrumentation().context.assets.open(name)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }

    private suspend fun importThenExport(importText: String): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = createTestDb(context)
        try {
            val exerciseRepository = ExerciseRepositoryImpl(RoomExerciseDatasource(db))
            val workoutRepository = WorkoutRepositoryImpl(RoomWorkoutDatasource(db))
            val fileReader = mockk<FileReader> {
                every { readFile(any()) } returns importText
            }
            var exportText: String? = null
            val fileWriter = mockk<FileWriter> {
                every { writeFile(any(), any()) } answers { exportText = secondArg() }
            }

            ImportWorkoutsUseCase(
                WorkoutParserImpl(), workoutRepository, exerciseRepository, fileReader,
                CreateVariationUseCase(exerciseRepository),
            )(ImportWorkoutsUseCase.Params.File(Uri.EMPTY)).getOrThrow()
            ExportWorkoutUseCase(workoutRepository, fileWriter)().getOrThrow()

            return checkNotNull(exportText) { "Export must write a file" }
        } finally {
            db.close()
        }
    }
}
