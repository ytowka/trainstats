package com.danilkha.trainstats.features.exercises

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.db.TrainStatsDb
import com.danilkha.trainstats.features.exercises.data.db.RoomExerciseDatasource
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseRepositoryImpl
import com.danilkha.trainstats.features.exercises.domain.model.*
import com.danilkha.trainstats.features.exercises.domain.usecase.CreateVariationUseCase
import com.danilkha.trainstats.features.settings.export.data.FileWriter
import com.danilkha.trainstats.features.settings.export.domain.ExportWorkoutUseCase
import com.danilkha.trainstats.features.settings.workoutimport.data.FileReader
import com.danilkha.trainstats.features.settings.workoutimport.data.WorkoutParserImpl
import com.danilkha.trainstats.features.settings.workoutimport.domain.ImportWorkoutsUseCase
import com.danilkha.trainstats.features.workout.data.db.RoomWorkoutDatasource
import com.danilkha.trainstats.features.workout.data.db.createTestDb
import com.danilkha.trainstats.features.workout.data.repository.WorkoutRepositoryImpl
import com.danilkha.trainstats.features.workout.domain.model.*
import com.danilkha.trainstats.features.workout.ui.toModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class ExerciseVariationDatabaseTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val pull = ExerciseData("pull", "тяга", null, false, true)
    private val narrow = ExerciseVariation("narrow", pull.id, "Узкая ручка")
    private val wide = ExerciseVariation("wide", pull.id, "Широкая ручка")
    private fun workout(variation: ExerciseVariation? = narrow, id: String = "workout") = Workout(
        id, Instant.parse("2026-10-10T12:00:00Z"), listOf(
            ExerciseSet("", id, pull, Repetitions.Single(10f), Kg(50f), 0, variation?.id, variation?.name),
        ), saved = true, archived = false,
    )

    @Test fun developmentSchemaResetCreatesAUsableDatabase() = runTest {
        val name = "exercise-variation-schema-reset-test"
        context.deleteDatabase(name)
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.danilkha.trainstats.db.TrainStatsDb/2.json").bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name).also { it.parentFile?.mkdirs() }, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                entity.optJSONArray("indices")?.let { indices ->
                    for (i in 0 until indices.length()) old.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val views = schema.getJSONArray("views")
            for (index in 0 until views.length()) {
                val view = views.getJSONObject(index)
                old.execSQL(view.getString("createSql").replace("\${VIEW_NAME}", view.getString("viewName")))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) old.execSQL(setup.getString(index))
            old.execSQL("INSERT INTO ExerciseEntity VALUES('pull','тяга',NULL,0,1,0)")
            old.execSQL("INSERT INTO WorkoutEntity VALUES('workout',1791633600000,1,'тяга',0,1791633600000)")
            old.execSQL("INSERT INTO ExerciseSetEntity VALUES('set','workout','pull','10.0',50,0,NULL)")
            old.version = 2
        }
        val db = Room.databaseBuilder(context, TrainStatsDb::class.java, name).fallbackToDestructiveMigration(true).build()
        try {
            val workouts = RoomWorkoutDatasource(db)
            assertTrue(workouts.getAll().isEmpty())
            val exercises = RoomExerciseDatasource(db)
            exercises.createExercise(pull)
            val variation = exercises.createVariation(pull.id, "Ручка 45°")
            workouts.saveWorkout(workout(variation))
            assertEquals(variation.id, workouts.getWorkoutById("workout").steps.single().variationId)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun namesAreUniqueWithinAnExerciseAndRenamingKeepsApproachReferences() = runTest {
        val db = createTestDb(context)
        try {
            val exercises = RoomExerciseDatasource(db)
            val workouts = RoomWorkoutDatasource(db)
            exercises.createExercise(pull.copy(variations = listOf(narrow, wide)))
            workouts.saveWorkout(workout())
            val createVariation = CreateVariationUseCase(ExerciseRepositoryImpl(exercises))
            val duplicate = createVariation(CreateVariationUseCase.Params(pull.id, " узКАЯ ручка "))
            assertTrue(duplicate.isFailure)
            assertTrue(runCatching { exercises.createExercise(pull) }.isFailure)
            assertEquals(listOf(narrow, wide), exercises.getExercise(pull.id).variations)
            val other = pull.copy(id = "other", name = "другое упражнение", variations = listOf(narrow.copy(id = "other-narrow", exerciseId = "other")))
            exercises.createExercise(other)
            exercises.createVariation(pull.id, "Обычный")
            // Swap names in one transaction without deleting/recreating the IDs.
            exercises.updateExercise(pull.copy(variations = listOf(narrow.copy(name = wide.name), wide.copy(name = narrow.name))))
            val approach = workouts.getWorkoutById("workout").steps.single()
            assertEquals(narrow.id, approach.variationId)
            assertEquals(wide.name, approach.variationName)
            assertTrue(runCatching { exercises.updateExercise(pull.copy(name = "changed", variations = listOf(narrow, wide.copy(name = narrow.name)))) }.isFailure)
            assertEquals(pull.name, exercises.getExercise(pull.id).name)
            assertEquals(wide.name, workouts.getWorkoutById("workout").steps.single().variationName)
        } finally { db.close() }
    }

    @Test fun deletionConvertsHistoricalSetsAndAStaleAutosaveCannotRestoreTheDeletedId() = runTest {
        val db = createTestDb(context)
        try {
            val exercises = RoomExerciseDatasource(db)
            val workouts = RoomWorkoutDatasource(db)
            exercises.createExercise(pull.copy(variations = listOf(narrow, wide)))
            workouts.saveWorkout(workout())
            workouts.saveWorkout(workout(id = "history"))
            val staleDraft = workouts.getWorkoutById("workout")
            exercises.updateExercise(pull.copy(variations = listOf(wide)))
            assertNull(workouts.getWorkoutById("history").steps.single().variationId)
            workouts.saveWorkout(staleDraft)
            assertNull(workouts.getWorkoutById("workout").steps.single().variationId)
            assertEquals(listOf(wide), exercises.getExercise(pull.id).variations)
        } finally { db.close() }
    }

    @Test fun savingAndDeletingADefaultPreservesIdsAndFallsBackToOrdinary() = runTest {
        val db = createTestDb(context)
        try {
            val exercises = RoomExerciseDatasource(db)
            exercises.createExercise(pull.copy(variations = listOf(narrow.copy(isDefault = true), wide)))
            assertEquals(listOf(narrow.id), exercises.getExercise(pull.id).variations.filter { it.isDefault }.map { it.id })
            assertTrue(exercises.getAllExercises().single().variations.first { it.id == narrow.id }.isDefault)
            assertTrue(exercises.findExercise(pull.name).single().variations.first { it.id == narrow.id }.isDefault)
            exercises.updateExercise(pull.copy(variations = listOf(narrow, wide.copy(isDefault = true))))
            assertEquals(listOf(wide.id), exercises.getExercise(pull.id).variations.filter { it.isDefault }.map { it.id })
            exercises.createVariation(pull.id, "Новая")
            assertEquals(listOf(wide.id), exercises.getExercise(pull.id).variations.filter { it.isDefault }.map { it.id })
            exercises.updateExercise(pull.copy(variations = listOf(narrow)))
            assertTrue(exercises.getExercise(pull.id).variations.none { it.isDefault })
        } finally { db.close() }
    }

    @Test fun exportImportRetainsVariationBlocksAndReusesExistingNormalizedNames() = runTest {
        val db = createTestDb(context)
        try {
            val exerciseRepo = ExerciseRepositoryImpl(RoomExerciseDatasource(db))
            val workoutRepo = WorkoutRepositoryImpl(RoomWorkoutDatasource(db))
            val parser = WorkoutParserImpl()
            val importer = ImportWorkoutsUseCase(parser, workoutRepo, exerciseRepo, mockk<FileReader>(), CreateVariationUseCase(exerciseRepo))
            val text = """
                10.10.2026
                тяга 45° (::) ${'$'} Узкая ${'$'}${'$'} ручка
                40кг x 10
                тяга 45° (::)
                35кг x 8
                тяга 45° (::) ${'$'} Обычный
                9
                ---
            """.trimIndent()
            importer(ImportWorkoutsUseCase.Params.Text(text)).getOrThrow()
            val exercise = exerciseRepo.getAllExercises().single()
            val used = exercise.variations.first { it.name.startsWith("Узкая") }
            exerciseRepo.createVariation(exercise.id, "Не использованная")
            var exported = ""
            val writer = mockk<FileWriter> { every { writeFile(any(), any()) } answers { exported = secondArg() } }
            ExportWorkoutUseCase(workoutRepo, writer)().getOrThrow()
            assertEquals(text.lineSequence().filter { it.isNotBlank() }.toList(), exported.lineSequence().filter { it.isNotBlank() }.toList())
            importer(ImportWorkoutsUseCase.Params.Text(exported.replace("Узкая", "УЗКАЯ"))).getOrThrow()
            assertEquals(3, exerciseRepo.getExercise(exercise.id).variations.size)
            val imported = workoutRepo.getAll()
            assertEquals(2, imported.size)
            imported.forEach { workout ->
                assertEquals(listOf(used.id, null, exercise.variations.first { it.name == "Обычный" }.id), workout.steps.map { it.variationId })
                var id = 0L
                assertEquals(3, workout.toModel { ++id }.groups.size)
            }
            val history = workoutRepo.getExerciseHistory(exercise.id)
            assertEquals(2, history.size)
            assertEquals("Узкая ${'$'} ручка", history.first().sets.first().variationName)
        } finally { db.close() }
    }
}
