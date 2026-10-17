package com.danilkha.trainstats.features.exercises.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danilkha.commoncore.utils.toLocal
import com.danilkha.trainstats.bottomsheet.rememberBottomSheetState
import com.danilkha.commoncore.utils.LocalDateFormat
import org.koin.compose.viewmodel.koinViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheet
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.components.Icon
import com.danilkha.commonds.theme.Colors
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheetArgs
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*

@Composable
fun ExerciseListScreenPage(
    viewModel: ExerciseListViewModel = koinViewModel()
) {
    val exerciseEditorBottomSheet = rememberBottomSheetState(onResult = {
        if(it[ExerciseEditorBottomSheetArgs.result] is ExerciseListEvent.UpdateList) {
            viewModel.processEvent(ExerciseListEvent.UpdateList)
        }
    })

    val state by viewModel.state.collectAsState()

    Box {
        ExerciseListScreen(
            state = state,
            onAddClicked = { exerciseEditorBottomSheet.show() },
            onExerciseClicked = {
                exerciseEditorBottomSheet.show(ExerciseEditorBottomSheetArgs.buildArgs(it.id))
            },
            onQueryChange = { viewModel.processEvent(ExerciseListEvent.ChangeSearchQuery(it)) }
        )
        ExerciseEditorBottomSheet(exerciseEditorBottomSheet)
    }
}

@Composable
fun ExerciseListScreen(
    state: ExerciseListState,
    onAddClicked: () -> Unit,
    onExerciseClicked: (ExerciseModel) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.background(color = Colors.background).fillMaxSize(),) {
        ExerciseSearchBar(
            query = state.searchQuery,
            onQueryChange = onQueryChange,
            onAddClicked = onAddClicked,)
        ExerciseList(
            items = state.exerciseList,
            onClick = {
                onExerciseClicked(it)
            }
        )
    }

}

@Composable
fun ExerciseList(
    items: List<ExerciseModel>,
    onClick: (ExerciseModel) -> Unit,
    onVariationClick: ((ExerciseModel, ExerciseVariation?) -> Unit)? = null,
){
    val state = rememberLazyListState()
    LaunchedEffect(items.size) {
        state.scrollToItem(0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = 10.dp,
            end = 10.dp,
            top = 10.dp,
            bottom = 60.dp
        ),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(items = items, key = { it.id }) { exercise ->
            ExerciseCard(
                exerciseModel = exercise,
                onClick = { onClick(exercise) },
                onVariationClick = onVariationClick?.let { select -> { variation -> select(exercise, variation) } },
            )
        }
    }
}

@Composable
fun ExerciseSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onAddClicked: () -> Unit,
){
    val focusManager = LocalFocusManager.current
    Card(
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ){
            GenericTextFiled(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .weight(1f),
                value = query,
                hint = stringResource(Res.string.search),
                onValueChange = onQueryChange,
                contentStart = {
                    Icon(imageVector = Icons.Default.Search)
                },
                contentEnd = if(query.isNotEmpty()) {
                    {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.clear),
                            onClick = {
                                onQueryChange("")
                                focusManager.clearFocus()
                            }
                        )
                    }
                } else null
            )
            Spacer(modifier = Modifier.size(10.dp))
            GenericButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentPaddings = PaddingValues(0.dp),
                color = Colors.background,
                onClick = onAddClicked
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.new_exercise)
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exerciseModel: ExerciseModel,
    onClick: () -> Unit,
    onVariationClick: ((ExerciseVariation?) -> Unit)? = null,
){
    Card(
        modifier = Modifier
            .fillMaxWidth().testTag("exercise_card_${exerciseModel.id}"),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Text(
            text = exerciseModel.name,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Colors.text,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if(exerciseModel.separated){
                Text(
                    text = stringResource(Res.string.split),
                    style = MaterialTheme.typography.body2
                )
            }
            Text(
                text = if(exerciseModel.hasWeight){
                    stringResource(Res.string.has_weight)
                }else stringResource(Res.string.with_body_weight),
                style = MaterialTheme.typography.body2,
                color = Colors.text,
            )
        }
        val lastUsedDate = exerciseModel.lastUsedDate
        if(lastUsedDate != null) {
            val dateFormat = LocalDateFormat.current
            Text(
                text = dateFormat.format(lastUsedDate.toLocal()),
                style = MaterialTheme.typography.caption,
                color = Colors.text.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (exerciseModel.variations.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("exercise_variations_${exerciseModel.id}"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item(key = "ordinary") {
                    ExerciseVariationChip(
                        name = stringResource(Res.string.ordinary_variation),
                        isDefault = exerciseModel.variations.none { it.isDefault },
                        modifier = Modifier.testTag("exercise_variation_${exerciseModel.id}_ordinary"),
                        onClick = onVariationClick?.let { select -> { select(null) } },
                    )
                }
                items(exerciseModel.variations, key = { "variation_${it.id}" }) { variation ->
                    ExerciseVariationChip(
                        name = variation.name, isDefault = variation.isDefault,
                        modifier = Modifier.testTag("exercise_variation_${exerciseModel.id}_${variation.id}"),
                        onClick = onVariationClick?.let { select -> { select(variation) } },
                    )
                }
            }
        }
    }
}


@Composable
private fun ExerciseVariationChip(
    name: String,
    isDefault: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier,
        ),
        shape = RoundedCornerShape(50),
        color = Colors.background,
        border = BorderStroke(1.dp, Colors.outline),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isDefault) androidx.compose.material.Icon(Icons.Default.Star,
                stringResource(Res.string.default_variation), tint = Colors.primary, modifier = Modifier.size(14.dp))
            Text(name, style = MaterialTheme.typography.body2, color = Colors.text)
        }
    }
}
