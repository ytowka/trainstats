package com.danilkha.trainstats.features.workout.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.db.TrainStatsDb
import java.io.InputStreamReader

fun createTestDb(context: Context): TrainStatsDb {
    val db = Room.inMemoryDatabaseBuilder(context, TrainStatsDb::class.java)
        .allowMainThreadQueries()
        .build()

    return db
}

suspend fun TrainStatsDb.populateDb() {
    val assets = InstrumentationRegistry.getInstrumentation().context.assets;

    val path = "trainstatsDb.sql"
    val inputStream = assets.open(path)

    val reader = InputStreamReader(inputStream)

    withTransaction {
        reader.readLines().forEach {
            openHelper.writableDatabase.execSQL(it)
        }
    }
}
