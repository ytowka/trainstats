package com.danilkha.trainstats.features.exercises.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.LocalDateFormat
import com.danilkha.commoncore.utils.format2
import com.danilkha.commonds.bottomsheet.BottomSheetScreen
import com.danilkha.commonds.bottomsheet.BottomSheetState
import com.danilkha.commonds.bottomsheet.initOnArgs
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import com.danilkha.commonds.components.BottomSheetContent
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.commonds.theme.TrainingStatsTheme
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.trainstats.features.exercises.ui.history.ExerciseHistoryBottomSheetArgs.exerciseIdArg
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch


object ExerciseHistoryBottomSheetArgs {

    const val exerciseIdArg = "exerciseId"

    fun buildArgs(exerciseId: Long): Map<String, Any> {
        return mapOf(exerciseIdArg to exerciseId)
    }
}

@Composable
fun ExerciseHistoryBottomSheet(
    sheetState: BottomSheetState
) {
    val viewModel = koinViewModel<ExerciseHistoryViewModel>()
    val state by viewModel.state.collectAsState()

    sheetState.initOnArgs { args ->
        val exerciseId = args[exerciseIdArg] as Long
        viewModel.init(exerciseId)
    }

    BottomSheetScreen(
        state = sheetState
    ) {
        ExerciseHistoryBottomSheet(
            state = state,
            onDismiss = { sheetState.hide() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseHistoryBottomSheet(
    state: ExerciseHistoryState,
    onDismiss: () -> Unit,
){
    BottomSheetContent(
        title = buildString {
            append(stringResource(id = R.string.history) )
            append(": ")
            append(state.exerciseName)

        },
        subtitle = stringResource(id = R.string.total_entries) +": "+state.list.size,
        onCloseClicked = { onDismiss() }
    ) {
        val pagerState = rememberPagerState(pageCount = { state.list.size })

        val coroutineScope = rememberCoroutineScope()

        HorizontalPager(
            modifier = Modifier
                .height(500.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            state = pagerState,
            contentPadding = PaddingValues(10.dp),
            verticalAlignment = Alignment.Top

        ) {
            val sets = state.list[it]
            ExerciseSetsHistoryCard(
                date = sets.date,
                index = state.list.size - it,
                sets = sets.sets
            )
        }
        Card(modifier = Modifier.padding(10.dp),) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.targetPage - 1)
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.targetPage + 1)
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(state.list.size-1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseSetsHistoryCard(
    date: LocalDate,
    index: Int,
    sets: List<ExerciseSetHistoryModel>,
){
    val dateFormat = LocalDateFormat.current

    Card(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                text = dateFormat.format(date),
                color = Colors.primary,
                style = ThemeTypography.title
            )

            Text(
                modifier = Modifier
                    .padding(end = 15.dp),
                text = index.toString(),
                style = ThemeTypography.subtitle
            )
        }

        Spacer(modifier = Modifier.size(20.dp))
        Column(
            modifier = Modifier
                .padding(top = 20.dp,
                    bottom = 0.dp,
                    end = 10.dp,
                    start = 10.dp)
                .fillMaxWidth(),
            //verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val textFieldStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Colors.text
            )

            val subtextSpanStyle = SpanStyle(
                fontWeight = FontWeight.Normal,
                color = Colors.text.copy(alpha = 0.6f),
                fontSize = 16.sp,
            )

            sets.forEachIndexed { index,  set ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        modifier = Modifier
                            .background(color = Colors.secondary, shape = CircleShape)
                            .size(25.dp)
                            .wrapContentHeight(Alignment.CenterVertically),
                        text = "${index+1}",
                        style = ThemeTypography.body2,
                        textAlign = TextAlign.Center,
                        color = Colors.textInverse
                    )
                    if(set.weight != null){
                        Text(
                            modifier = Modifier
                                .weight(1f),
                            text = buildAnnotatedString {
                                append(set.weight.value.format2())
                                withStyle(subtextSpanStyle){
                                    append(stringResource(id = R.string.kg))
                                }
                            },
                            style = textFieldStyle,
                            color = Colors.text
                        )
                    }
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = buildAnnotatedString {
                            when(set.reps){
                                is RepetitionsModel.Double -> {
                                    append(set.reps.left?.format2())
                                    append(", ")
                                    append(set.reps.right?.format2())
                                    append(" ")
                                }
                                is RepetitionsModel.Single -> {
                                    append(set.reps.reps?.format2())
                                }
                            }
                            withStyle(subtextSpanStyle){
                                append(stringResource(id = R.string.reps))
                            }
                        },
                        style = textFieldStyle,
                        color = Colors.text
                    )
                }
                Divider()
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun ExerciseHistoryBottomSheetPreview(){
    TrainingStatsTheme {
        val state = ExerciseHistoryState(
            exerciseName = "Жим лежа",
            list = listOf(
                ExerciseHistoryModel(
                    date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    sets = listOf(
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Single(10f),
                            weight = Kg(20f)
                        ),
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Single(10f),
                            weight = Kg(20f)
                        ),
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Single(10f),
                            weight = Kg(20f)
                        )
                    )
                ),
                ExerciseHistoryModel(
                    date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    sets = listOf(
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Double(10f, 10f),
                            weight = Kg(20f)
                        ),
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Double(10f, 10f),
                            weight = Kg(20f)
                        ),
                        ExerciseSetHistoryModel(
                            id = 0,
                            workoutId = 1,
                            reps = RepetitionsModel.Double(10f, 10f),
                            weight = Kg(20f)
                        )
                    )
                )
            )
        )
        ExerciseHistoryBottomSheet(
            state = state,
            onDismiss = {}
        )
    }
}