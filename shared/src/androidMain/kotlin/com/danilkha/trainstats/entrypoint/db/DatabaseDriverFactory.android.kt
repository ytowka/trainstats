package com.danilkha.trainstats.entrypoint.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createBuilder(): RoomDatabase.Builder<TrainStatsDb> {
        return Room.databaseBuilder<TrainStatsDb>(
            context,
            TrainStatsDb::class.java,
            TrainStatsDb.DB_NAME
        )
            .fallbackToDestructiveMigration(true)
    }
}
