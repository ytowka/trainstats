package com.danilkha.trainstats.entrypoint.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual class DatabaseDriverFactory {
    actual fun createBuilder(): RoomDatabase.Builder<TrainStatsDb> {
        return Room.databaseBuilder<TrainStatsDb>(
            TrainStatsDb.DB_NAME
        )
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(true)
    }
}
