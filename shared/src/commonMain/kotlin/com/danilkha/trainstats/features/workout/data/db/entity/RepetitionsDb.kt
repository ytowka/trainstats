package com.danilkha.trainstats.features.workout.data.db.entity

import androidx.room.TypeConverter
import com.danilkha.trainstats.features.workout.domain.model.Repetitions

sealed interface RepetitionsDb {
    data class Single(val reps: Float) : RepetitionsDb
    data class Double(val left: Float, val right: Float) : RepetitionsDb
}

fun RepetitionsDb.toDomain(): Repetitions{
    return when(this){
        is RepetitionsDb.Double -> Repetitions.Double(left, right)
        is RepetitionsDb.Single -> Repetitions.Single(reps)
    }
}

fun Repetitions.toEntity(): RepetitionsDb{
    return when(this){
        is Repetitions.Double -> RepetitionsDb.Double(left, right)
        is Repetitions.Single -> RepetitionsDb.Single(reps)
    }
}

class RepetitionsDbTypeConverter{

    @TypeConverter
    fun toDb(model: RepetitionsDb?): String?{
        return when(model){
            is RepetitionsDb.Double -> "${model.left}:${model.right}"
            is RepetitionsDb.Single -> model.reps.toString()
            null -> null
        }
    }

    @TypeConverter
    fun toModel(string: String?): RepetitionsDb?{
        val reps = string?.split(":")
        return if(reps != null){
            if (reps.size == 1){
                RepetitionsDb.Single(reps[0].toFloat())
            }else{
                RepetitionsDb.Double(reps[0].toFloat(), reps[1].toFloat())
            }
        }else null
    }
}