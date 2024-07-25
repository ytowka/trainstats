package com.danilkha.trainstats.features.workout.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.danilkha.trainstats.features.workout.data.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutEntity
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("select * from workoutentity where archived = 0 order by dateTime desc")
    fun getWorkoutHistory(): Flow<List<WorkoutEntity>>

    @Query("select * from workoutentity where id = :id")
    @Transaction
    fun getWorkoutById(id: Long): WorkoutWithExercises

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWorkout(workoutEntity: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun saveSets(exerciseSetEntity: List<ExerciseSetEntity>)

    @Query("delete from exercisesetentity where workoutId = :workoutId")
    fun deleteWorkoutExercises(workoutId: Long)

    @Transaction
    fun updateWorkout(
        workoutEntity: WorkoutEntity,
        sets: List<ExerciseSetEntity>
    ): Long{
        val workoutId = saveWorkout(workoutEntity)
        if(workoutEntity.id != 0L){
            deleteWorkoutExercises(workoutEntity.id)
            val newSets = sets.map {
                it.copy(workoutId = workoutId)
            }
            saveSets(newSets)
        }
        return workoutId
    }

    @Query("update workoutentity set saved = 1 where id = :workoutId")
    fun commitWorkoutSave(workoutId: Long)

    @Query("update workoutentity set archived = 1 where id = :id")
    fun archiveWorkout(id: Long)
}