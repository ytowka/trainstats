package com.danilkha.trainstats.features.exercises.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.danilkha.commonds.theme.PreviewContent

private class ExerciseListScreenPreviewProvider : PreviewParameterProvider<ExerciseListState> {
    override val values: Sequence<ExerciseListState> = sequenceOf(
        ExerciseListState(
            searchQuery = "",
            exerciseList = listOf(
                ExerciseModel(
                    id = "",
                    name = "жим лежа",
                    separated = false,
                    imageUrl = null,
                    hasWeight = true
                )
            )
        )
    )

}

@Composable
@Preview
private fun ExerciseListScreenPreview(@PreviewParameter(ExerciseListScreenPreviewProvider::class) state: ExerciseListState){
    PreviewContent {
        ExerciseListScreen(
            state = state,
            onAddClicked = {  },
            onExerciseClicked = {},
            onQueryChange = {}
        )
    }
}
