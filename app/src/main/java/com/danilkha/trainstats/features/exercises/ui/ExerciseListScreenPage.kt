package com.danilkha.trainstats.features.exercises.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.trainstats.features.exercises.ui.editor.ExerciseEditorBottomSheet
import com.danilkha.uikit.bottomsheet.rememberBottomSheetController
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.Fab
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.GenericTextFiled
import com.danilkha.uikit.components.Icon
import com.danilkha.uikit.theme.Colors

@Composable
fun ExerciseListScreenPage(
    viewModel: ExerciseListViewModel = getCurrentViewModel { it.exerciseListViewModel }
) {
    val exerciseEditor = rememberBottomSheetController(bottomSheetClass = ExerciseEditorBottomSheet::class.java)

    val state by viewModel.state.collectAsState()

    ExerciseListScreen(
        state = state,
        onAddClicked = {exerciseEditor.show()},
        onExerciseClicked = {
            exerciseEditor.show(ExerciseEditorBottomSheet.buildArgs(it.id))
        },
        onQueryChange = viewModel::queryUpdated
    )
}

@Composable
fun ExerciseListScreen(
    state: ExerciseListState,
    onAddClicked: () -> Unit,
    onExerciseClicked: (ExerciseModel) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),) {
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
    onClick: (ExerciseModel) -> Unit
){
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 10.dp,
            end = 10.dp,
            top = 10.dp,
            bottom = 60.dp
        ),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(items = items , key = { it.id }){
            ExerciseCard(
                exerciseModel =  it,
                onClick = { onClick(it) }
            )
        }
    }
}

@Composable
fun ExerciseSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onAddClicked: () -> Unit,
){
    Card(
        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ){
            GenericTextFiled(
                modifier = Modifier.weight(1f),
                value = query,
                onValueChange = onQueryChange,
                contentStart ={
                    Icon(imageVector = Icons.Default.Search)
                }
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
                    imageVector = Icons.Default.Add
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exerciseModel: ExerciseModel,
    onClick: () -> Unit,
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                    text = stringResource(id = R.string.split),
                    style = MaterialTheme.typography.body2
                )
            }
            Text(
                text = if(exerciseModel.hasWeight){
                    stringResource(id = R.string.has_weight)
                }else stringResource(id = R.string.with_body_weight),
                style = MaterialTheme.typography.body2,
                color = Colors.text,
            )
        }
    }
}
