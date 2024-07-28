package com.danilkha.trainstats.features.workout.ui.editor

import com.danilkha.trainstats.features.workout.domain.usecase.DeleteWorkoutUseCase
import com.danilkha.trainstats.features.workout.domain.usecase.SaveWorkoutUseCase
import com.danilkha.trainstats.features.workout.domain.model.WorkoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSaver @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
){
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val currentState = MutableStateFlow<WorkoutParams?>(null)

    init {
        scope.launch {
            currentState
                .debounce(FLUSH_DEBOUNCE)
                .filterNotNull()
                .collect{
                    commitInternal(it)
                }
        }
    }

    fun update(workout: WorkoutParams){
        currentState.value = workout
    }

    fun commit(workout: WorkoutParams){
        currentState.value = null
        scope.launch {
            if(workout.steps.isEmpty()){
                workout.id?.let { deleteWorkoutUseCase(it) }
            }else{
                commitInternal(workout)
            }
        }
    }

    private suspend fun commitInternal(workout: WorkoutParams){
        saveWorkoutUseCase(workout)
    }
}

private const val FLUSH_DEBOUNCE = 10_000L //ms