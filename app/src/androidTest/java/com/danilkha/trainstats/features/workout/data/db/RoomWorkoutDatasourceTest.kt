package com.danilkha.trainstats.features.workout.data.db

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomWorkoutDatasourceTest {

    private lateinit var roomWorkoutDatasource: RoomWorkoutDatasource

    @Before
    fun setup() = runTest{
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = createTestDb(context)
        db.populateDb()

        roomWorkoutDatasource = RoomWorkoutDatasource(db.workoutDao())
    }


    @Test
    fun testHistoryByExercise() = runTest{
        val exerciseId = "3"

        roomWorkoutDatasource.getExerciseHistory(exerciseId)
    }
}