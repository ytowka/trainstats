package com.danilkha.trainstats.features.exercises.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.danilkha.uikit.theme.TrainingStatsTheme

private class ExerciseListScreenPreviewProvider : PreviewParameterProvider<ExerciseListState> {
    override val values: Sequence<ExerciseListState> = sequenceOf(
        ExerciseListState(
            searchQuery = "",
            exerciseList = listOf(
                ExerciseModel(
                    id = 1,
                    name = "жим лежа",
                    separated = false,
                    hasWeight = true
                )
            )
        )
    )

}

@Composable
@Preview
private fun ExerciseListScreenPreview(@PreviewParameter(ExerciseListScreenPreviewProvider::class) state: ExerciseListState){
    TrainingStatsTheme {
        ExerciseListScreen(
            state = state,
            onAddClicked = {  }
        )
    }
}
