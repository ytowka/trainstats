package com.danilkha.trainstats.features.workout.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import java.io.InputStreamReader

suspend fun createPrepopulatedDb(context: Context): TrainStatsDb{
    val db = Room.inMemoryDatabaseBuilder(context, TrainStatsDb::class.java)
        .allowMainThreadQueries()
        .build()

    val assets = InstrumentationRegistry.getInstrumentation().context.assets;

    println( assets.list("")?.joinToString())

    val path = "trainstatsDb.sql"
    val inputStream = assets.open(path)

    val reader = InputStreamReader(inputStream)

    db.withTransaction {
        reader.readLines().forEach {
            db.openHelper.writableDatabase.execSQL(it)
        }
    }

    return db
}
