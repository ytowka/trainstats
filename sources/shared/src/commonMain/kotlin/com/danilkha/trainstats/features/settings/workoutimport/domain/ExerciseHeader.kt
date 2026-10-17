package com.danilkha.trainstats.features.settings.workoutimport.domain

/** A doubled dollar is literal; the first single dollar separates the variation. */
fun exerciseHeader(name: String, variationName: String?): String =
    name.replace("$", "$$") + (variationName?.let { " $ " + it.replace("$", "$$") } ?: "")

fun parseExerciseHeader(line: String): Pair<String, String?> {
    val name = StringBuilder()
    val variation = StringBuilder()
    var separated = false
    var index = 0
    while (index < line.length) {
        val char = line[index++]
        if (char == '$' && index < line.length && line[index] == '$') {
            (if (separated) variation else name).append('$')
            index++
        } else if (char == '$' && !separated) separated = true
        else (if (separated) variation else name).append(char)
    }
    return name.toString().trim() to variation.toString().trim().takeIf { it.isNotEmpty() }
}
