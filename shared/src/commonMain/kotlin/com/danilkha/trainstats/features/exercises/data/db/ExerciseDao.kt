package com.danilkha.trainstats.features.exercises.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData

@Dao
interface ExerciseDao {

    @Query("""
 select * from ExerciseEntity as e
 left join ExerciseCountView as stats on stats.exerciseId = e.id
 where archived = 0
 order by stats.inWorkouts desc

        """)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    suspend fun getAllExercises(): List<ExerciseWithLastUsed>

    @Query("""
 select * from ExerciseEntity as e
 left join ExerciseCountView as stats on stats.exerciseId = e.id
 where name like '%' || :query || '%' and archived = 0
 order by stats.inWorkouts desc
        """)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    suspend fun getAllExercises(query: String): List<ExerciseWithLastUsed>

    @Query("select * from ExerciseEntity where id = :id")
    suspend fun getExercise(id: Long): ExerciseEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("update ExerciseEntity set archived = 1 where id = :id")
    suspend fun deleteExercise(id: Long)

    @Query("select e.id, e.name from ExerciseEntity as e where e.name in (:names)")
    suspend fun getExerciseIds(names: List<String>): List<ExerciseEntity.NameId>
}