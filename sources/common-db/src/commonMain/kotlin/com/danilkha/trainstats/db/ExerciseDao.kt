package com.danilkha.trainstats.db

import androidx.room.Upsert
import com.danilkha.trainstats.db.entity.ExerciseVariationEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import com.danilkha.trainstats.db.entity.ExerciseEntity
import com.danilkha.trainstats.db.entity.ExerciseWithLastUsed

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
    suspend fun getExercise(id: String): ExerciseEntity

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createExercise(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("update ExerciseEntity set archived = 1 where id = :id")
    suspend fun deleteExercise(id: String)

    @Query("select e.id, e.name from ExerciseEntity as e where e.name in (:names)")
    suspend fun getExerciseIds(names: List<String>): List<ExerciseEntity.NameId>

    @Query("SELECT * FROM ExerciseVariationEntity WHERE exerciseId = :exerciseId ORDER BY rowid")
    suspend fun getVariations(exerciseId: String): List<ExerciseVariationEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVariation(variation: ExerciseVariationEntity)

    @Upsert
    suspend fun upsertVariations(variations: List<ExerciseVariationEntity>)

    @Query("DELETE FROM ExerciseVariationEntity WHERE exerciseId = :exerciseId AND id NOT IN (:retainedIds)")
    suspend fun deleteMissingVariations(exerciseId: String, retainedIds: List<String>)

    @Query("UPDATE ExerciseVariationEntity SET name = :temporaryPrefix || id WHERE exerciseId = :exerciseId")
    suspend fun prepareVariationsUpdate(exerciseId: String, temporaryPrefix: String)

    @Query("SELECT id FROM ExerciseVariationEntity WHERE id IN (:ids)")
    suspend fun getExistingVariationIds(ids: List<String>): List<String>
}
