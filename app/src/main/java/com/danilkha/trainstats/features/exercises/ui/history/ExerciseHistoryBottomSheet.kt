package com.danilkha.trainstats.features.exercises.ui.history

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.core.os.bundleOf
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.format
import com.danilkha.trainstats.core.utils.format2
import com.danilkha.trainstats.core.viewmodel.viewModel
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import com.danilkha.trainstats.features.workout.ui.Side
import com.danilkha.uikit.bottomsheet.ComposeContextBottomDialog
import com.danilkha.uikit.components.BottomSheetContent
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.bottomSheetShape
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography
import com.danilkha.uikit.theme.TrainingStatsTheme
import korlibs.time.Date
import korlibs.time.DateTime
import korlibs.time.DateTimeTz
import korlibs.time.days

class ExerciseHistoryBottomSheet : ComposeContextBottomDialog(){

    val viewModel by viewModel { it.exerciseHistoryViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getLong(EXERCISE_ID_ARG)?.let {
            viewModel.init(it)
            arguments?.remove(EXERCISE_ID_ARG)
        }
    }

    override val content: @Composable () -> Unit = {

        val state by viewModel.state.collectAsState()
        ExerciseHistoryBottomSheet(
            state = state,
            onDismiss = ::dismiss
        )
    }

    companion object{
        private const val EXERCISE_ID_ARG = "exercise_id_arg"

        fun buildArgs(exerciseId: Long): Bundle = bundleOf(
            EXERCISE_ID_ARG to exerciseId
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseHistoryBottomSheet(
    state: ExerciseHistoryState,
    onDismiss: () -> Unit,
){
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface, shape = bottomSheetShape)
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildString {
                    append(stringResource(id = R.string.history) )
                    append(": ")
                    append(state.exerciseName)

                },
                modifier = Modifier.weight(1f),
                style = ThemeTypography.title
            )
            Icon(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { onDismiss() },
                ),
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = stringResource(id = R.string.total_entries) +": "+state.list.size,
            style = ThemeTypography.body1.copy(fontWeight = FontWeight.Normal)
        )
        val pagerState = rememberPagerState(pageCount = { state.list.size })

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
                index = it + 1,
                sets = sets.sets
            )
        }
    }

}

@Composable
fun ExerciseSetsHistoryCard(
    date: Date,
    index: Int,
    sets: List<ExerciseSetHistoryModel>,
){
    Card(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth(),
        backgroundColor = Colors.background,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                text = date.format(),
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

        Spacer(modifier = Modifier.size(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            //verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 0.dp,
                end = 10.dp,
                start = 10.dp
            )
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
                    date = DateTimeTz.nowLocal().local.date,
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
                    date = (DateTimeTz.nowLocal().local - 2.days).date,
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