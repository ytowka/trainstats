package com.danilkha.trainstats.di

import android.content.Context
import androidx.room.Room
import com.danilkha.trainstats.entrypoint.db.TrainStatsDb
import com.danilkha.trainstats.features.exercises.data.db.ExerciseDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule {
    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): TrainStatsDb{
        return Room.databaseBuilder(context, TrainStatsDb::class.java, "trainstatsDb")
            .build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(db: TrainStatsDb): ExerciseDao{
        return db.exerciseDao
    }
}