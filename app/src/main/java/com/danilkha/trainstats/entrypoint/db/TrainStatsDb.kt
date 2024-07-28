package com.danilkha.trainstats.entrypoint.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.danilkha.trainstats.features.exercises.data.db.ExerciseCountView
import com.danilkha.trainstats.features.exercises.data.db.ExerciseDao
import com.danilkha.trainstats.features.exercises.data.db.ExerciseEntity
import com.danilkha.trainstats.features.workout.data.db.WorkoutDao
import com.danilkha.trainstats.features.workout.data.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutEntity

@Database(
entities = [
    ExerciseEntity::class,
    WorkoutEntity::class,
    ExerciseSetEntity::class
],
    views = [
        ExerciseCountView::class
            ],
    version = 1, exportSchema = true)
@TypeConverters(DateTimeConverter::class, StringListConverter::class)
abstract class TrainStatsDb : RoomDatabase(){


    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
}