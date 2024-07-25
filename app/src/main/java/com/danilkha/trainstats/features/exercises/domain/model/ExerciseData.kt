package com.danilkha.trainstats.features.exercises.domain.model

data class ExerciseData(
    val id: Long = 0,
    val name: String,
    val imageUrl: String?,
    val separated: Boolean,
    val hasWeight: Boolean,
){

    companion object{
        fun stub(id: Long, name: String) = ExerciseData(
            id = id,
            name = name,
            imageUrl = null,
            separated = false,
            hasWeight = false
        )
    }
}
