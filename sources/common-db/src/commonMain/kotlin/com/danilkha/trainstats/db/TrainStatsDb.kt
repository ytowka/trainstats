package com.danilkha.trainstats.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.danilkha.trainstats.db.entity.ExerciseCountView
import com.danilkha.trainstats.db.entity.ExerciseEntity
import com.danilkha.trainstats.db.entity.ExerciseLastUsedView
import com.danilkha.trainstats.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.db.entity.WorkoutEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        ExerciseSetEntity::class
    ],
    views = [
        ExerciseCountView::class,
        ExerciseLastUsedView::class
            ],
    version = 1, exportSchema = true)
@ConstructedBy(TrainStatsDbConstructor::class)
@TypeConverters(DateTimeConverter::class, StringListConverter::class)
abstract class TrainStatsDb : RoomDatabase(){


    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val DB_NAME = "trainstatsDb"
    }
}

@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
expect object TrainStatsDbConstructor : RoomDatabaseConstructor<TrainStatsDb> {
    override fun initialize(): TrainStatsDb
}
