package com.danilkha.trainstats.features.workout.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.danilkha.trainstats.features.workout.data.db.entity.ExerciseSetEntity
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutEntity
import com.danilkha.trainstats.features.workout.data.db.entity.WorkoutWithExercises
import com.danilkha.trainstats.features.workout.data.db.entity.exercises.ExerciseWorkoutRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("select * from workoutentity where archived = 0 order by dateTime desc")
    fun getWorkoutHistory(): Flow<List<WorkoutEntity>>

    @Query("""
select * from workoutentity where archived = 0 order by dateTime desc
    """)
    suspend fun getAll(): List<WorkoutWithExercises>

    @Query("select * from workoutentity where id = :id")
    @Transaction
    suspend fun getWorkoutById(id: Long): WorkoutWithExercises

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkout(workoutEntity: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveSets(exerciseSetEntity: List<ExerciseSetEntity>)

    @Query("delete from exercisesetentity where workoutId = :workoutId")
    suspend fun deleteWorkoutExercises(workoutId: Long)

    @Transaction
    suspend fun updateWorkout(
        workoutEntity: WorkoutEntity,
        sets: List<ExerciseSetEntity>
    ): Long{
        val workoutId = saveWorkout(workoutEntity)
        deleteWorkoutExercises(workoutEntity.id)
        val newSets = sets.map {
            it.copy(workoutId = workoutId)
        }
        saveSets(newSets)
        return workoutId
    }

    @Query("update workoutentity set saved = 1 where id = :workoutId")
    suspend fun commitWorkoutSave(workoutId: Long)

    @Query("update workoutentity set archived = 1 where id = :id")
    suspend fun archiveWorkout(id: Long)

    @Query("delete from workoutentity where id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("""
select s.*, w.dateTime, e.name as exerciseName  from ExerciseSetEntity as s
inner join WorkoutEntity w on w.id = s.workoutId
inner join ExerciseEntity e on e.id = s.exerciseId
where exerciseId = :exerciseId
order by dateTime desc
    """)
    suspend fun getHistoryByExercise(exerciseId: Long): List<ExerciseWorkoutRelation>
}