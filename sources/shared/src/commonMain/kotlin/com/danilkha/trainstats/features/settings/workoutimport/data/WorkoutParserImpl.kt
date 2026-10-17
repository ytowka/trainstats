package com.danilkha.trainstats.features.settings.workoutimport.data

import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.settings.workoutimport.domain.WorkoutParser
import com.danilkha.trainstats.features.settings.workoutimport.domain.parseExerciseHeader
import com.danilkha.trainstats.features.workout.domain.model.*
import kotlinx.datetime.LocalDate

class WorkoutParserImpl : WorkoutParser {
    override fun parse(text: String): Pair<List<ExerciseData>, List<WorkoutParams>> {
        val exercises = linkedMapOf<String, ExerciseData>()
        val workouts = mutableListOf<WorkoutParams>()
        var date: LocalDate? = null
        var exerciseName = ""
        var variationName: String? = null
        val sets = mutableListOf<SetParams>()
        fun finish() {
            date?.let { workouts.add(WorkoutParams(id = null, date = it, steps = sets.toList())) }
            date = null
            exerciseName = ""
            variationName = null
            sets.clear()
        }
        text.lineSequence().forEachIndexed { lineIndex, raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEachIndexed
            if (separator.matches(line)) { finish(); return@forEachIndexed }
            try {
                if (date == null) {
                    val parts = line.split('.')
                    require(parts.size == 3)
                    val year = parts[2].toInt().let { if (it < 2000) it + 2000 else it }
                    date = LocalDate(year, parts[1].toInt(), parts[0].toInt())
                } else {
                    val match = weightedSet.matchEntire(line)
                    val reps = singleSet.matchEntire(line)
                    if (match != null || reps != null) {
                        require(exerciseName.isNotBlank())
                        val repetitions = if (match != null) parseReps(match.groupValues[2]) else parseReps(reps!!.groupValues[1])
                        val weight = match?.groupValues?.get(1)?.replace(',', '.')?.toFloat()?.let(::Kg)
                        require(weight == null || weight.value.isFinite())
                        sets.add(SetParams("", exerciseName, repetitions, weight, variationName = variationName))
                        val previous = exercises[exerciseName]
                        exercises[exerciseName] = ExerciseData(
                            id = "", name = exerciseName, imageUrl = null,
                            separated = previous?.separated == true || repetitions is Repetitions.Double,
                            hasWeight = previous?.hasWeight == true || weight != null,
                        )
                    } else {
                        val header = parseExerciseHeader(line)
                        require(header.first.isNotBlank())
                        exerciseName = header.first.lowercase()
                        variationName = header.second
                    }
                }
            } catch (error: Exception) {
                throw ParserException(lineIndex)
            }
        }
        finish()
        return exercises.values.toList() to workouts
    }

    private fun parseReps(text: String): Repetitions {
        val parts = text.split(',').map { it.trim().replace(',', '.').toFloat() }
        require(parts.all { it.isFinite() })
        return if (parts.size == 1) Repetitions.Single(parts[0]) else Repetitions.Double(parts[0], parts[1])
    }

    companion object {
        private val separator = Regex("---+")
        private const val NUMBER = "[0-9]+(?:\\.[0-9]+)?"
        private const val REPS = "$NUMBER(?:\\s*,\\s*$NUMBER)?"
        private val weightedSet = Regex("([0-9]+(?:[.,][0-9]+)?)\\s*(?:[кk]г|kg)?\\s*[xх×]\\s*($REPS)(?:\\s*(?:повт|раз))?", RegexOption.IGNORE_CASE)
        private val singleSet = Regex("($REPS)(?:\\s*(?:повт|раз))?", RegexOption.IGNORE_CASE)
    }
}

class ParserException(val invalidLineIndex: Int) : Exception("cannot parse line at $invalidLineIndex")
