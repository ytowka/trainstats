package com.danilkha.workout

import androidx.compose.runtime.Immutable

@Immutable
data class WorkoutState(
    val items: List<Item>
)


sealed class Item{

    class Initializing() : Item()

    class ExerciseInfo() : Item()

    class Set() : Item()

    class EditingSet() : Item()

    class Rest() : Item()

    class EditingRest() : Item()

    class NewSet() : Item()
}