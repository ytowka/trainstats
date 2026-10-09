package com.danilkha.trainstats

import androidx.lifecycle.viewModelScope
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.trainstats.features.exercises.domain.ExerciseRepository
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseData
import com.danilkha.trainstats.features.stats.domain.*
import com.danilkha.trainstats.features.stats.ui.*
import com.danilkha.trainstats.features.workout.domain.WorkoutRepository
import com.danilkha.trainstats.features.workout.domain.model.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StatisticsTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf
    val press = ExerciseData("press", "Жим лёжа", null, separated = false, hasWeight = true)
    val pullup = ExerciseData("pullup", "Подтягивания", null, separated = false, hasWeight = false)
    val date = Instant.parse("2026-10-09T09:00:00Z")
    fun set(position: Int, exercise: ExerciseData = press, weight: Float? = 40f, reps: Repetitions = Repetitions.Single(10f)) =
        ExerciseSet("set-$position", "workout", exercise, reps, weight?.let(::Kg), position)
    fun workout(id: String = "workout", sets: List<ExerciseSet> = listOf(set(0))) =
        Workout(id, date, sets, saved = true, archived = false)

    Given("workouts with drafts, archives, mixed exercises and unordered sets") {
        val later = workout("later", listOf(set(0))).copy(dateTime = Instant.parse("2026-10-10T09:00:00Z"))
        val earlier = workout(sets = listOf(set(4, weight = 50f), set(2, pullup), set(1, weight = 30f)))
        When("building exercise statistics") {
            val points = exerciseStatistics(listOf(later, earlier, workout("draft", emptyList()).copy(saved = false), workout("archived").copy(archived = true)), press.id)
            Then("active workouts with sets appear in chronological order") {
                points.map { it.workoutId } shouldBe listOf("workout", "workout", "later")
            }
            Then("set numbers follow exercise order across groups and reset each workout") {
                points.map { it.setNumber } shouldBe listOf(1, 2, 1)
                points.map { it.weight } shouldBe listOf(30f, 50f, 40f)
            }
        }
    }
    Given("two workouts with identical timestamps") {
        Then("their points retain distinct workout identities") {
            exerciseStatistics(listOf(workout("b"), workout("a")), press.id).map { it.workoutId } shouldBe listOf("a", "b")
        }
    }
    Given("history persisted by the current editor with saved=false") {
        Then("its approaches still appear on the graph") {
            exerciseStatistics(listOf(workout().copy(saved = false)), press.id).map { it.weight } shouldBe listOf(40f)
        }
    }
    Given("an exercise without history") {
        Then("statistics are empty") {
            exerciseStatistics(emptyList(), press.id) shouldBe emptyList()
            exerciseStatistics(listOf(workout()), pullup.id) shouldBe emptyList()
        }
    }
    Given("missing weight and explicitly zero weight") {
        val points = exerciseStatistics(listOf(workout(sets = listOf(set(0, weight = null), set(1, weight = 0f)))), press.id)
        Then("missing values stay absent while zero remains a chartable value") {
            points.map { it.weight } shouldBe listOf(null, 0f)
            points.map { it.volume } shouldBe listOf(null, 0f)
            points.map { it.repetitions } shouldBe listOf(10f, 10f)
        }
    }
    Given("single and separate left/right repetitions") {
        val points = exerciseStatistics(listOf(workout(sets = listOf(set(0), set(1, reps = Repetitions.Double(8f, 12f))))), press.id)
        Then("volume uses all repetitions and the repetition chart averages the two sides") {
            points.map { it.repetitions } shouldBe listOf(10f, 10f)
            points.map { it.volume } shouldBe listOf(400f, 800f)
        }
    }
    Given("a chart with several years of history") {
        Then("the initial viewport covers the latest six calendar months") {
            val points = exerciseStatistics(listOf(workout("old").copy(dateTime = Instant.parse("2023-01-01T09:00:00Z")), workout()), press.id)
            val range = statisticsChartRange(points)
            range.initialFirst.toLocal().date.toString().substring(0, 7) shouldBe "2026-04"
            (range.initialZoom > 1f) shouldBe true
            (range.initialStart > 0f) shouldBe true
            range.first shouldBe points.first().date - kotlin.time.Duration.parse("12h")
        }
    }
    Given("one workout at the end of March") {
        Then("the six-month range clamps to the last day of September") {
            val points = exerciseStatistics(listOf(workout().copy(dateTime = Instant.parse("2026-03-31T00:00:00Z"))), press.id)
            val range = statisticsChartRange(points)
            range.initialFirst.toLocal().date.toString() shouldBe "2025-09-30"
            range.initialZoom shouldBe 1f
        }
    }

    val exerciseRepository = mockk<ExerciseRepository>()
    val workoutRepository = mockk<WorkoutRepository>()
    val useCase = GetStatisticsUseCase(exerciseRepository, workoutRepository)
    val models = mutableListOf<StatisticsViewModel>()
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    fun viewModel() = StatisticsViewModel(useCase).also { models.add(it) }

    Given("the statistics data loader") {
        Then("it preserves the repository usage ranking and includes persisted history with saved=false") {
            coEvery { exerciseRepository.getAllExercises() } returns listOf(pullup, press)
            coEvery { workoutRepository.getAll() } returns listOf(workout().copy(saved = false), workout("draft", emptyList()).copy(saved = false), workout("archive").copy(archived = true))
            val data = useCase(Unit).getOrThrow()
            data.exercises.map { it.id } shouldBe listOf(pullup.id, press.id)
            data.workouts.map { it.id } shouldBe listOf("workout")
        }
    }
    Given("loaded statistics state") {
        val vm = viewModel()
        val data = StatisticsData(listOf(pullup, press), listOf(workout()))
        val loaded = vm.reduce(StatisticsState(), StatisticsEvent.Loaded(data))
        Then("initial selection prefers an exercise with history") {
            loaded.exerciseId shouldBe press.id
        }
        Then("search ignores case and surrounding whitespace") {
            vm.reduce(loaded, StatisticsEvent.Search("  жИМ  ")).filteredExercises.map { it.id } shouldBe listOf(press.id)
        }
        Then("selecting a bodyweight exercise switches to repetitions and clears the old point") {
            val selected = vm.reduce(loaded, StatisticsEvent.SelectPoint(loaded.points.first()))
            val changed = vm.reduce(selected, StatisticsEvent.SelectExercise(pullup.id))
            changed.metric shouldBe StatisticsMetric.Repetitions
            changed.selectedWorkout shouldBe null
            changed.points shouldBe emptyList()
        }
        Then("metric changes clear the selection") {
            val selected = vm.reduce(loaded, StatisticsEvent.SelectPoint(loaded.points.first()))
            vm.reduce(selected, StatisticsEvent.SelectMetric(StatisticsMetric.Volume)).selectedPoint shouldBe null
        }
        Then("hiding a set clears its selection and excludes it from point navigation") {
            val selected = vm.reduce(loaded, StatisticsEvent.SelectPoint(loaded.points.first()))
            val hidden = vm.reduce(selected, StatisticsEvent.ToggleSet(1))
            hidden.hiddenSets shouldBe setOf(1)
            hidden.selectedPoint shouldBe null
            hidden.visiblePoints shouldBe emptyList()
            vm.reduce(hidden, StatisticsEvent.ToggleSet(1)).visiblePoints shouldBe loaded.points
        }
        Then("changing exercises restores visibility of all sets") {
            val hidden = vm.reduce(loaded, StatisticsEvent.ToggleSet(1))
            vm.reduce(hidden, StatisticsEvent.SelectExercise(pullup.id)).hiddenSets shouldBe emptySet()
        }
        Then("returning from workout details preserves a selected point while a deleted workout clears it") {
            val selected = vm.reduce(loaded, StatisticsEvent.SelectPoint(loaded.points.first()))
            vm.reduce(selected, StatisticsEvent.Loaded(data)).selectedWorkout?.id shouldBe "workout"
            vm.reduce(selected, StatisticsEvent.Loaded(data.copy(workouts = emptyList()))).selectedPoint shouldBe null
        }
    }
    Given("a failed load followed by retry") {
        Then("the screen leaves loading, exposes an error and can recover") {
            runTest {
                coEvery { exerciseRepository.getAllExercises() } throws IllegalStateException("unavailable")
                val vm = viewModel()
                val flow = vm.state
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
                runCurrent()
                flow.value.loading shouldBe false
                flow.value.failed shouldBe true
                coEvery { exerciseRepository.getAllExercises() } returns listOf(press)
                coEvery { workoutRepository.getAll() } returns listOf(workout())
                vm.processEvent(StatisticsEvent.Retry)
                runCurrent()
                flow.value.failed shouldBe false
                flow.value.exerciseId shouldBe press.id
                flow.value.points.size shouldBe 1
            }
        }
    }
})
