package com.danilkha.trainstats.entrypoint.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.danilkha.trainstats.features.exercises.data.db.ExerciseDao
import com.danilkha.trainstats.features.exercises.data.db.ExerciseEntity

@Database(entities = [
    ExerciseEntity::class
], version = 1, exportSchema = true)
abstract class TrainStatsDb : RoomDatabase(){


    abstract val exerciseDao: ExerciseDao
}