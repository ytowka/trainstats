package com.danilkha.trainstats.db

import androidx.room.RoomDatabase

expect class DatabaseDriverFactory {
    fun createBuilder(): RoomDatabase.Builder<TrainStatsDb>
}
