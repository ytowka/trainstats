package com.danilkha.trainstats.features.profile.data

import android.util.Log
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.profile.domain.WorkoutParser
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.domain.model.Repetitions
import com.danilkha.trainstats.features.workout.domain.model.SetParams
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import korlibs.time.Date
import javax.inject.Inject

class WorkoutParserImpl @Inject constructor(): WorkoutParser {
    override fun parse(text: String):  Pair<List<ExerciseData>,List<WorkoutParams>> {
        val exercises = mutableListOf<ExerciseData>()
        val paramsList = mutableListOf<WorkoutParams>()

        val addedExerciseNames = mutableSetOf<String>()

        val entries = text.split(ENTRY_SEPARATOR)
        var totalLines = 0
        entries.forEach {
            if(it.isNotBlank()){
                val (params, lines) = parseWorkout(it, totalLines)
                totalLines += lines
                params.steps.forEach { step ->
                    if(step.exerciseName !in addedExerciseNames){
                        val isSeparated = step.reps is Repetitions.Double
                        val hasWeight = step.weight != null
                        val exerciseDate = ExerciseData(
                            id = 0,
                            name = step.exerciseName,
                            imageUrl = null,
                            separated = isSeparated,
                            hasWeight = hasWeight
                        )
                        exercises.add(exerciseDate)
                        addedExerciseNames.add(step.exerciseName)
                    }
                }
                paramsList.add(params)
            }
        }

        return exercises to paramsList
    }

    private fun parseWorkout(string: String, totalLines: Int): Pair<WorkoutParams, Int>{
        val lines = string.split(Regex("\n+"))
        val dateString = lines.first().split(".")
        var lastExerciseName = ""
        val exerciseSets = mutableListOf<SetParams>()
        lines.subList(1, lines.size).forEachIndexed { index, it ->
            try {
                if(exerciseNameRegex.matches(it)){
                    lastExerciseName = it.lowercase().trim()
                }else if(setRegex.matches(it)){
                    val setParams = it.split(setSeparatorRegex)
                    val set = if(setParams.size == 1){
                        val reps = setParams[0].split(" ")[0]
                        SetParams(
                            exerciseId = 0,
                            exerciseName = lastExerciseName,
                            reps = Repetitions.Single(reps.toFloat()),
                            weight = null
                        )
                    }else{
                        val weight = setParams.first().replace(",", ".").toFloat()
                        val repsParts = setParams[1].split(repsSeparatorRegex)
                        val reps = if(repsParts.size == 1){

                            Repetitions.Single(repsParts[0].toFloatOrNull() ?: 0f)
                        }else{

                            val left = repsParts[0].toFloatOrNull() ?: 0f
                            val right = repsParts[1].toFloatOrNull() ?: 0f
                            Repetitions.Double(left, right)
                        }
                        SetParams(
                            exerciseId = 0,
                            exerciseName = lastExerciseName,
                            reps = reps,
                            weight = Kg(weight)
                        )
                    }
                    exerciseSets.add(set)
                }
            }catch (e: Exception){
                throw ParserException(totalLines + index + 1)
            }
        }
        try {
            val day = dateString[0].toInt()
            val month = dateString[1].toInt()
            val year = dateString[2].toInt().let {
                if(it < 2000){
                    it + 2000
                }else{
                    it
                }
            }
            return WorkoutParams(
                id = null,
                date = Date(
                    day = day,
                    year = year,
                    month = month
                ),
                steps = exerciseSets.toList()
            ) to lines.size
        } catch (e: Exception){
            throw ParserException(totalLines)
        }
    }

    companion object{
        private val ENTRY_SEPARATOR = Regex("\n+---+\n\n+")

        private val dateRegex = Regex("[0-9]+\\.[0-9]+\\.[0-9]+")
        private val exerciseNameRegex = Regex("[A-zА-яё ]+")
        private val setRegex = Regex("[0-9][0-9A-zА-я,. ]*")

        private val setSeparatorRegex = Regex(" *([ек]г)? *[xх] *")
        private val repsSeparatorRegex = Regex(" *, *")
    }
}

class ParserException(val invalidLineIndex: Int) : Exception("cannot parse line at $invalidLineIndex")