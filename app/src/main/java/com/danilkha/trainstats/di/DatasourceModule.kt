package com.danilkha.trainstats.di

import com.danilkha.trainstats.features.exercises.data.db.RoomExerciseDatasource
import com.danilkha.trainstats.features.exercises.data.repository.ExerciseLocalDatasource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DatasourceModule {

    @Binds
    @Singleton
    abstract fun exerciseLocalDs(roomExerciseDatasource: RoomExerciseDatasource) : ExerciseLocalDatasource
}