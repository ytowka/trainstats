package com.danilkha.trainstats.features.workout.data.db

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var workoutDao: WorkoutDao
    private lateinit var db: TrainStatsDb


    @Before
    fun createDb() = runTest{
        val context = ApplicationProvider.getApplicationContext<Context>()

        try {
            db = createPrepopulatedDb(context)

            workoutDao = db.workoutDao()
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testHistoryByExercise() = runBlocking{
        val result = workoutDao.getHistoryByExercise(3)

        val workouts = result.groupBy { it.workoutId }
        workouts.forEach {
            println(it)
        }
        println()
    }
}