package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import com.danilkha.trainstats.features.exercises.domain.model.validVariationNames
import com.danilkha.trainstats.features.exercises.ui.components.VariationOptionCard
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.*

@Composable
internal fun ExerciseVariationsEditor(
    variations: List<ExerciseVariation>,
    onAdd: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        if (variations.isNotEmpty()) {
            Text(stringResource(Res.string.variations), style = ThemeTypography.subtitle)
            Spacer(Modifier.height(12.dp))
            VariationOptionCard(
                name = stringResource(Res.string.ordinary_variation),
                modifier = Modifier.testTag("exercise_editor_ordinary_variation"),
            )
            Spacer(Modifier.height(8.dp))
        }
        variations.forEach { variation ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GenericTextFiled(
                    modifier = Modifier.weight(1f), value = variation.name,
                    onValueChange = { onRename(variation.id, it) },
                    hint = stringResource(Res.string.variation_name),
                )
                IconButton(onClick = { onDelete(variation.id) }, modifier = Modifier.testTag("delete_variation_${variation.id}")) {
                    Icon(Icons.Default.Delete, stringResource(Res.string.delete), tint = Colors.error)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        TextButton(onClick = onAdd) { Text(stringResource(Res.string.add_variation), color = Colors.primary) }
        if (!validVariationNames(variations)) {
            Text(stringResource(Res.string.variation_names_invalid), color = Colors.error)
        }
    }
}
