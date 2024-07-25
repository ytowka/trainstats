package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

@Dao
interface ExerciseDao {

    @Query("select * from ExerciseEntity")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("select * from ExerciseEntity where id = :id")
    suspend fun getExercise(id: Long): ExerciseEntity

    @Query("select * from ExerciseEntity where name like '%'+:query+'%' ")
    suspend fun findExercise(query: String): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("delete from ExerciseEntity where id = :id")
    suspend fun deleteExercise(id: Long)
}