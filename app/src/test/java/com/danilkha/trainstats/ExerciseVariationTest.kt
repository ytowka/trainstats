package com.danilkha.trainstats

import androidx.lifecycle.viewModelScope
import com.danilkha.trainstats.features.exercises.data.FakeExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.*
import com.danilkha.trainstats.features.exercises.domain.usecase.*
import com.danilkha.trainstats.features.exercises.ui.editor.*
import com.danilkha.trainstats.features.exercises.ui.selector.*
import com.danilkha.trainstats.features.settings.workoutimport.data.WorkoutParserImpl
import com.danilkha.trainstats.features.settings.workoutimport.domain.exerciseHeader
import com.danilkha.trainstats.features.stats.domain.*
import com.danilkha.trainstats.features.stats.ui.*
import com.danilkha.trainstats.features.workout.domain.model.*
import com.danilkha.trainstats.features.workout.ui.*
import com.danilkha.trainstats.features.workout.ui.editor.*
import com.danilkha.trainstats.features.workout.ui.exercisehistory.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.coEvery
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.usecase.GetWorkoutByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExerciseVariationTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf
    val narrow = ExerciseVariation("narrow", "pull", "Узкая ручка")
    val wide = ExerciseVariation("wide", "pull", "Широкая ручка")
    val pull = ExerciseData("pull", "Тяга", null, false, true, variations = listOf(narrow, wide))
    val date = Instant.parse("2026-10-10T12:00:00Z")
    fun set(index: Int, variation: ExerciseVariation? = null, weight: Float = 50f, reps: Float = 10f) =
        ExerciseSet("set-$index", "workout", pull, Repetitions.Single(reps), Kg(weight), index,
            variationId = variation?.id, variationName = variation?.name)
    fun workout(sets: List<ExerciseSet>) = Workout("workout", date, sets, saved = false, archived = false)

    Given("ordinary and named variation blocks interleaved in global order") {
        val workout = workout(listOf(set(3, wide), set(0), set(1, narrow), set(2, narrow), set(4)))
        Then("only adjacent identical pairs merge and each set inherits its block when saved") {
            val repository = mockk<WorkoutRepository>()
            coEvery { repository.getWorkoutById(workout.id) } returns workout
            val ordered = GetWorkoutByIdUseCase(repository)(workout.id).getOrThrow()
            var id = 0L
            val model = ordered.toModel { ++id }
            model.groups.map { it.variationId } shouldBe listOf(null, narrow.id, wide.id, null)
            model.groups.map { it.sets.filterIsInstance<ExerciseSetSlot.ExerciseSetModel>().size } shouldBe listOf(1, 2, 1, 1)
            WorkoutState(initialWorkout = model, groups = model.groups).mapToParams().steps.map { it.variationId } shouldBe
                listOf(null, narrow.id, narrow.id, wide.id, null)
        }
    }
    Given("ordinary record 100 and narrow record 70 with a zero-repetition approach") {
        val history = listOf(workout(listOf(set(0, weight = 100f), set(1, narrow, 65f), set(2, narrow, 90f, 0f), set(3, narrow, 70f))))
        Then("filtering renumbers first, preserves the zero slot, and computes filtered maxima and record") {
            val points = exerciseStatistics(history, pull.id, setOf(narrow.id))
            points.map { it.setNumber } shouldBe listOf(1, 3)
            maximumStatisticsPoint(points, StatisticsMetric.Weight)?.weight shouldBe 70f
            maximumStatisticsPoints(points, StatisticsMetric.Weight).size shouldBe 1
            val state = StatisticsState(data = StatisticsData(listOf(pull), history), exerciseId = pull.id,
                selectedVariationIds = setOf(narrow.id), visibleSeries = emptySet())
            state.recordPoint?.weight shouldBe 70f
            state.recordPoint?.variationId shouldBe narrow.id
            state.recordPoint?.variationName shouldBe narrow.name
            points.map { it.variationId } shouldBe listOf(narrow.id, narrow.id)
            state.visiblePoints shouldBe emptyList()
            state.copy(selectedVariationIds = null).recordPoint?.weight shouldBe 100f
            state.copy(selectedVariationIds = emptySet()).points shouldBe emptyList()
        }
        Then("frequency counts a workout once, and counts only valid selected approaches") {
            val today = LocalDate(2026, 10, 12)
            fun frequency(ids: Set<String?>?, scope: FrequencyScope = FrequencyScope.Exercise) =
                frequencyStatistics(history, pull.id, scope, FrequencyInterval.Week, today, TimeZone.UTC, ids)
            frequency(setOf(narrow.id)).allTime shouldBe FrequencyTotals(1, 2)
            frequency(null).allTime shouldBe FrequencyTotals(1, 3)
            frequency(emptySet()).allTime shouldBe FrequencyTotals()
            frequency(emptySet(), FrequencyScope.AllWorkouts).allTime shouldBe FrequencyTotals(1, 3)
        }
    }
    Given("a shared statistics filter") {
        Then("tabs and frequency scope retain checkboxes; switching exercises resets them") {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val vm = StatisticsViewModel(mockk())
                try {
                    var state = StatisticsState(data = StatisticsData(listOf(pull), emptyList()), exerciseId = pull.id)
                    state = vm.reduce(state, StatisticsEvent.ToggleVariation(null))
                    state.activeVariationIds shouldBe setOf(narrow.id, wide.id)
                    state = vm.reduce(state, StatisticsEvent.SelectTab(StatisticsTab.Frequency))
                    state = vm.reduce(state, StatisticsEvent.SelectFrequencyScope(FrequencyScope.AllWorkouts))
                    state = vm.reduce(state, StatisticsEvent.SelectFrequencyScope(FrequencyScope.Exercise))
                    state.activeVariationIds shouldBe setOf(narrow.id, wide.id)
                    state = vm.reduce(state, StatisticsEvent.SelectAllVariations(false))
                    state.needsVariationSelection shouldBe true
                    state = vm.reduce(state, StatisticsEvent.SelectExercise("other"))
                    state.needsVariationSelection shouldBe false
                    state.activeVariationIds shouldBe setOf(null)
                } finally { vm.viewModelScope.cancel(); advanceUntilIdle(); Dispatchers.resetMain() }
            }
        }
    }
    Given("an open workout editor with several approaches") {
        Then("selecting retains the ID and name for the whole block and ordinary clears them") {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val vm = WorkoutViewModel(mockk(relaxed = true), mockk(), mockk(), mockk())
                try {
                    var id = 0L
                    val model = workout(listOf(set(0), set(1))).toModel { ++id }
                    var state = WorkoutState(initialWorkout = model, groups = model.groups)
                    state = vm.reduce(state, WorkoutEvent.SelectVariation(model.groups.first().groupTempId, narrow.id, narrow.name))
                    state.mapToParams().steps.map { it.variationId } shouldBe listOf(narrow.id, narrow.id)
                    state.groups.first().variationName shouldBe narrow.name
                    state = vm.reduce(state, WorkoutEvent.SelectVariation(model.groups.first().groupTempId, null, null))
                    state.groups.first().variationName shouldBe null
                    state.mapToParams().steps.map { it.variationId } shouldBe listOf(null, null)
                } finally { vm.viewModelScope.cancel(); advanceUntilIdle(); Dispatchers.resetMain() }
            }
        }
    }
    Given("history with mixed variations") {
        val sets = listOf(
            ExerciseSetHistoryModel("1", "w", RepetitionsModel.Single(10f), null),
            ExerciseSetHistoryModel("2", "w", RepetitionsModel.Single(10f), null, narrow.id, narrow.name),
        )
        val state = ExerciseHistoryState(list = listOf(ExerciseHistoryModel(LocalDate(2026, 10, 10), sets)))
        Then("history defaults to all, and current null means only ordinary approaches") {
            state.visibleList.first().sets.size shouldBe 2
            state.copy(onlyCurrentVariation = true).visibleList.first().sets.map { it.id } shouldBe listOf("1")
            state.copy(onlyCurrentVariation = true, currentVariationId = narrow.id).visibleList.first().sets.map { it.id } shouldBe listOf("2")
        }
    }
    Given("names normalized within one exercise") {
        Then("Russian case and surrounding spaces collide while a user-defined Обычный is valid") {
            validVariationNames(listOf(narrow, wide.copy(name = " узкАЯ ручка "))) shouldBe false
            validVariationNames(listOf(narrow, wide.copy(name = "Обычный"))) shouldBe true
            validVariationNames(listOf(narrow.copy(name = " "))) shouldBe false
        }
    }
    Given("variation names entered in arbitrary case") {
        Then("create, update and separate creation store the same canonical spelling") {
            val repo = FakeExerciseRepository()
            CreateExercisesUseCase(repo)(pull.copy(variations = listOf(narrow.copy(name = "  уЗКАЯ ручка  ")))).getOrThrow()
            val id = pull.id
            repo.getExercise(id).variations.single() shouldBe narrow
            UpdateExercisesUseCase(repo)(pull.copy(variations = listOf(narrow.copy(name = "  нОВОЕ имя ")))).getOrThrow()
            repo.getExercise(id).variations.single() shouldBe narrow.copy(name = "Новое имя")
            CreateVariationUseCase(repo)(CreateVariationUseCase.Params(id, "  шИРОКАЯ ручка ")).getOrThrow().name shouldBe wide.name
        }
    }
    Given("exercise editor changes including a confirmed deletion") {
        Then("reopening discards all drafts and Save applies the list with stable IDs") {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val repo = FakeExerciseRepository()
                repo.createExercise(pull)
                val vm = ExerciseEditorViewModel(GetExercisesUseCase(repo), CreateExercisesUseCase(repo), UpdateExercisesUseCase(repo), DeleteExercisesUseCase(repo))
                val flow = vm.state
                val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
                try {
                    vm.processEvent(ExerciseEditorEvent.Init(pull.id)); advanceUntilIdle()
                    vm.processEvent(ExerciseEditorEvent.RemoveVariation(narrow.id))
                    vm.processEvent(ExerciseEditorEvent.RenameVariation(wide.id, "Новое имя"))
                    vm.processEvent(ExerciseEditorEvent.AddVariation); advanceUntilIdle()
                    repo.getExercise(pull.id).variations shouldBe listOf(narrow, wide)
                    vm.processEvent(ExerciseEditorEvent.Init(pull.id)); advanceUntilIdle()
                    flow.value.variations shouldBe listOf(narrow, wide)
                    vm.processEvent(ExerciseEditorEvent.RemoveVariation(narrow.id))
                    vm.processEvent(ExerciseEditorEvent.RenameVariation(wide.id, "Новое имя")); advanceUntilIdle()
                    vm.processEvent(ExerciseEditorEvent.Save); advanceUntilIdle()
                    repo.getExercise(pull.id).variations shouldBe listOf(wide.copy(name = "Новое имя"))
                    vm.processEvent(ExerciseEditorEvent.Init(null)); advanceUntilIdle()
                    flow.value.variations shouldBe emptyList()
                    flow.value.mode shouldBe ExerciseEditorMode.New
                } finally { collector.cancel(); vm.viewModelScope.cancel(); advanceUntilIdle(); Dispatchers.resetMain() }
            }
        }
    }
    Given("a variation being created from a workout") {
        Then("typing and cancelling changes no dictionary; Create persists and emits the selection") {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val repo = FakeExerciseRepository()
                repo.createExercise(pull)
                val vm = VariationSelectorViewModel(repo, CreateVariationUseCase(repo))
                val flow = vm.state
                val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
                val selections = mutableListOf<VariationSelected>()
                val effects = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.sideEffects.collect { selections.add(it) } }
                try {
                    vm.processEvent(VariationSelectorEvent.Init(pull.id)); advanceUntilIdle()
                    vm.processEvent(VariationSelectorEvent.Add)
                    vm.processEvent(VariationSelectorEvent.EditName("Ручка 45° :: ($)")); advanceUntilIdle()
                    repo.getExercise(pull.id).variations.size shouldBe 2
                    vm.processEvent(VariationSelectorEvent.Init(pull.id)); advanceUntilIdle()
                    flow.value.name shouldBe null
                    vm.processEvent(VariationSelectorEvent.Add)
                    vm.processEvent(VariationSelectorEvent.EditName("Ручка 45° :: ($)")); advanceUntilIdle()
                    vm.processEvent(VariationSelectorEvent.Create); advanceUntilIdle()
                    val created = repo.getExercise(pull.id).variations.last()
                    created.name shouldBe "Ручка 45° :: ($)"
                    selections.last().variation shouldBe created
                } finally { collector.cancel(); effects.cancel(); vm.viewModelScope.cancel(); advanceUntilIdle(); Dispatchers.resetMain() }
            }
        }
    }
    Given("old and new export text with punctuation and escaped dollars") {
        Then("the parser accepts all headers, an empty suffix is null, and Обычный stays named") {
            val (exercises, workouts) = WorkoutParserImpl().parse("""
                10.10.2026
                тяга 45° (::)
                40кг x 10
                тяга 45° (::) ${'$'} Узкая ручка 45° ::
                35кг x 8
                тяга 45° (::) ${'$'}
                10, 12
                тяга 45° (::) ${'$'} Обычный
                9
                ${exerciseHeader("тест ${'$'} 45°", "вариация ${'$'} (::)")}
                20кг x 8, 10
                ---
            """.trimIndent())
            exercises.map { it.name } shouldBe listOf("тяга 45° (::)", "тест ${'$'} 45°")
            workouts.single().steps.map { it.variationName } shouldBe listOf(null, "Узкая ручка 45° ::", null, "Обычный", "вариация ${'$'} (::)")
            workouts.single().steps[2].reps shouldBe Repetitions.Double(10f, 12f)
        }
    }
})
