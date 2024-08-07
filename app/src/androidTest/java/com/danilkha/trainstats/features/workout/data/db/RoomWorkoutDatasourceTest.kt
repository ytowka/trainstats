package com.danilkha.trainstats.features.workout.data.db

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danilkha.trainstats.core.utils.format
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
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

        val db = createPrepopulatedDb(context)

        roomWorkoutDatasource = RoomWorkoutDatasource(db.workoutDao())
    }


    @Test
    fun testHistoryByExercise() = runTest{
        val exerciseId = 3L

        roomWorkoutDatasource.getExerciseHistory(exerciseId)
    }
}