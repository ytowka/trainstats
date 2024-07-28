package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

@Dao
interface ExerciseDao {

    @Query("select * from ExerciseEntity where archived = 0")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("select * from ExerciseEntity where id = :id")
    suspend fun getExercise(id: Long): ExerciseEntity

    @Query("select * from ExerciseEntity where name like '%'+:query+'%' and archived = 0")
    suspend fun findExercise(query: String): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("update ExerciseEntity set archived = 1 where id = :id")
    suspend fun deleteExercise(id: Long)

    @Query("select e.id, e.name from ExerciseEntity as e where e.name in (:names)")
    suspend fun getExerciseIds(names: List<String>): List<ExerciseEntity.NameId>
}