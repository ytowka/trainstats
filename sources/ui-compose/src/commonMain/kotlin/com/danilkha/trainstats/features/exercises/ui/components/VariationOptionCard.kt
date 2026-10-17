package com.danilkha.trainstats.features.exercises.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography

@Composable
internal fun VariationOptionCard(
    name: String,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        modifier = modifier.fillMaxWidth().clip(shape).then(
            if (selected != null) Modifier.selectable(selected, role = Role.RadioButton, onClick = onClick) else Modifier,
        ),
        shape = shape,
        backgroundColor = if (selected == true) Colors.selectionBackground else Colors.surface,
        strokeColor = if (selected == true) Colors.primary else Colors.outline,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 24.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(name, Modifier.weight(1f), color = if (selected == true) Colors.primary else Colors.text,
                style = ThemeTypography.body1.copy(fontWeight = if (selected == true) FontWeight.SemiBold else FontWeight.Medium))
            if (selected != null) RadioButton(
                selected = selected, onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Colors.primary, unselectedColor = Colors.text.copy(alpha = .45f)),
            )
        }
    }
}
