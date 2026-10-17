package com.danilkha.trainstats.features.exercises.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.trainstats.features.exercises.domain.model.ExerciseVariation
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ExerciseVariationsEditor(
    variations: List<ExerciseVariation>,
    onAdd: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDefault: (String?) -> Unit,
    invalidIds: Set<String>,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (variations.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.variations), Modifier.weight(1f), style = ThemeTypography.subtitle)
                Text(stringResource(Res.string.default_variation), style = MaterialTheme.typography.caption,
                    color = Colors.text.copy(alpha = .55f))
            }
            Card(
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("exercise_editor_ordinary_variation"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = Colors.text.copy(alpha = .06f).compositeOver(Colors.surface),
            ) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.ordinary_variation), Modifier.weight(1f),
                        style = ThemeTypography.body1, color = Colors.text.copy(alpha = .45f))
                    DefaultVariationButton(
                        name = stringResource(Res.string.ordinary_variation),
                        selected = variations.none { it.isDefault },
                        modifier = Modifier.testTag("default_variation_ordinary"),
                        onClick = { onDefault(null) },
                    )
                }
            }
        }
        variations.forEach { variation -> key(variation.id) {
            val deleteLabel = stringResource(Res.string.delete)
            val dismissState = rememberDismissState(confirmStateChange = {
                if (it == DismissValue.DismissedToStart) onDelete(variation.id)
                // Confirmation keeps the draft intact; cancellation restores the row.
                false
            })
            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.fillMaxWidth().testTag("variation_editor_row_${variation.id}")
                    .semantics { customActions = listOf(CustomAccessibilityAction(deleteLabel) { onDelete(variation.id); true }) },
                directions = setOf(DismissDirection.EndToStart),
                dismissThresholds = { FractionalThreshold(.35f) },
                background = {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        .background(Colors.error).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterEnd) {
                        Icon(Icons.Default.Delete, null, tint = Colors.onError)
                    }
                },
                dismissContent = {
                    GenericTextFiled(
                        modifier = Modifier.fillMaxWidth().height(48.dp), value = variation.name,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        onValueChange = { onRename(variation.id, it) },
                        hint = stringResource(Res.string.variation_name),
                        contentEnd = {
                            DefaultVariationButton(
                                name = variation.name.ifBlank { stringResource(Res.string.variation_name) },
                                selected = variation.isDefault,
                                modifier = Modifier.testTag("default_variation_${variation.id}"),
                                onClick = { onDefault(if (variation.isDefault) null else variation.id) },
                            )
                        },
                    )
                },
            )
        } }
        TextButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onAdd
        ) {
            Text(stringResource(Res.string.add_variation), color = Colors.primary)
        }
        if (invalidIds.isNotEmpty()) {
            Text(stringResource(Res.string.variation_names_invalid), color = Colors.error,
                modifier = Modifier.testTag("variation_names_error"))
        }
    }
}

@Composable
private fun DefaultVariationButton(
    name: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier.size(28.dp).semantics { this.selected = selected }) {
        Icon(if (selected) Icons.Default.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(Res.string.make_default_variation, name),
            tint = if (selected) Colors.primary else Colors.text.copy(alpha = .3f), modifier = Modifier.size(20.dp))
    }
}
