package com.danilkha.trainstats.features.workout.ui.details

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.viewmodel.MviViewModel
import com.danilkha.trainstats.features.workout.domain.model.Workout
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutByIdUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

data class WorkoutDetailsState(
    val id: String = "",
    val workout: Workout? = null,
    val loading: Boolean = true,
    val failed: Boolean = false,
)

sealed interface WorkoutDetailsEvent {
    data class Load(val id: String) : WorkoutDetailsEvent
    data class Loaded(val id: String, val workout: Workout?) : WorkoutDetailsEvent
}

class WorkoutDetailsViewModel(
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
) : MviViewModel<WorkoutDetailsState, WorkoutDetailsEvent, Nothing>() {
    override val startState = WorkoutDetailsState()
    private var loadJob: Job? = null

    override fun reduce(state: WorkoutDetailsState, event: WorkoutDetailsEvent): WorkoutDetailsState = when (event) {
        is WorkoutDetailsEvent.Load -> WorkoutDetailsState(id = event.id)
        is WorkoutDetailsEvent.Loaded -> if (state.id == event.id)
            state.copy(workout = event.workout, loading = false, failed = event.workout == null)
        else state
    }

    override suspend fun afterReduce(newState: WorkoutDetailsState, event: WorkoutDetailsEvent) {
        if (event is WorkoutDetailsEvent.Load) {
            loadJob?.cancel()
            loadJob = viewModelScope.launch {
                val result = getWorkoutByIdUseCase(event.id)
                coroutineContext.ensureActive()
                processEvent(WorkoutDetailsEvent.Loaded(event.id, result.getOrNull()))
            }
        }
    }
}
