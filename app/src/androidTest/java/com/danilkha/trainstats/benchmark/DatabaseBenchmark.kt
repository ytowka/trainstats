package com.danilkha.trainstats.benchmark

import android.app.Instrumentation
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun databaseCreation() {
        benchmarkRule.measureRepeated {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            Room.databaseBuilder(appContext, TrainStatsDb::class.java, "trainstatsDb")
                .createFromAsset("trainstatsDb.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}