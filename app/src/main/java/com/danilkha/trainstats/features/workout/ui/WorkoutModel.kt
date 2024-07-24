package com.danilkha.trainstats.features.workout.ui

import androidx.compose.runtime.Immutable
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.workout.domain.model.ExerciseSet
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Workout
import korlibs.time.DateTime

data class WorkoutModel(
    val id: Long,
    val dateTime: DateTime,
    val groups: List<ExerciseGroup>,
    val saved: Boolean,
)

data class ExerciseGroup(
    val groupTempId: Long = 0,
    val exerciseId: Long = 0,
    val name: String,
    val imageUrl: String?,
    val hasWeight: Boolean,
    val separated: Boolean,
    val sets: List<ExerciseSetSlot>,
)

sealed class ExerciseSetSlot(
    open val tempId: Long
){
    data class ExerciseSetModel(
        override val tempId: Long,
        val reps: RepetitionsModel,
        val weight: Kg?,
    ) : ExerciseSetSlot(tempId)

    class Stub(override val tempId: Long) : ExerciseSetSlot(tempId)
}





const val SET_DELETE_DELAY = 5000L //ms

enum class Side { Left, Right }

inline fun Workout.toModel(
    idProvider: () -> Long,
) : WorkoutModel{
    val groups = mutableListOf<ExerciseGroup>()

    var lastExerciseId: Long? = null
    val lastGroupSets = mutableListOf<ExerciseSetSlot>()
    var lastGroup: ExerciseGroup? = null
    steps.forEach { step ->
        if(step.exerciseData.id != lastExerciseId){
            lastGroup?.let { group ->
                lastGroupSets.add(ExerciseSetSlot.Stub(idProvider()))
                groups.add(group.copy(
                    sets = lastGroupSets.toList()
                ))
            }
            lastGroup = ExerciseGroup(
                groupTempId = idProvider(),
                exerciseId = step.exerciseData.id,
                name = step.exerciseData.name,
                imageUrl = step.exerciseData.imageUrl,
                hasWeight = step.exerciseData.hasWeight,
                separated = step.exerciseData.separated,
                sets = emptyList()
            )
            lastGroupSets.clear()
        }
        lastExerciseId = step.exerciseData.id
        val set = ExerciseSetSlot.ExerciseSetModel(
            tempId = idProvider(),
            reps = step.reps.toModel(),
            weight = step.weight,
        )
        lastGroupSets.add(set)
    }
    lastGroup?.let { group ->
        lastGroupSets.add(ExerciseSetSlot.Stub(idProvider()))
        groups.add(group.copy(
            sets = lastGroupSets.toList()
        ))
    }

    return WorkoutModel(
        id = id,
        dateTime = dateTime,
        groups = groups.toList(),
        saved = saved
    )
}
