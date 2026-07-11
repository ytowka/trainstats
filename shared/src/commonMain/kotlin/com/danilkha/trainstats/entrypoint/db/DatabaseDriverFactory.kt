package com.danilkha.trainstats.entrypoint.db

import androidx.room.RoomDatabase

expect class DatabaseDriverFactory {
    fun createBuilder(): RoomDatabase.Builder<TrainStatsDb>
}
