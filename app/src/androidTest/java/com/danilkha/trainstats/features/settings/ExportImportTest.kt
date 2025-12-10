package com.danilkha.trainstats.features.settings

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
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
import java.io.InputStreamReader

class ExportImportTest {

    @Test
    fun exportImportTest() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets;

        val parser = WorkoutParserImpl()

        val db = createTestDb(context)

        val exerciseDatasource = RoomExerciseDatasource(db.exerciseDao())
        val exerciseRepository = ExerciseRepositoryImpl(exerciseDatasource)

        val workoutDatasource = RoomWorkoutDatasource(db.workoutDao())
        val workoutRepository = WorkoutRepositoryImpl(workoutDatasource)


        val importText = InputStreamReader(
            assets.open("workout_export.txt")
        ).readText()
        val fileReader = mockk<FileReader> {
            every { readFile(any()) } returns importText
        }

        var actualExportText = ""
        val fileWriter = mockk<FileWriter> {
            every { writeFile(any(), any()) } answers {
                actualExportText = this.secondArg()
            }
        }

        ImportWorkoutsUseCase(parser, workoutRepository, exerciseRepository, fileReader)
            .invoke(ImportWorkoutsUseCase.Params.File(Uri.EMPTY))

        ExportWorkoutUseCase(workoutRepository, fileWriter)
            .invoke()

        actualExportText.trim() shouldBe importText.trim()


    }
}