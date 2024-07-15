package com.danilkha.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.Paddings
import com.danilkha.uikit.theme.ThemeTypography

@Composable
fun GenericButton(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPaddings: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    color: Color = MaterialTheme.colors.secondary,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
){
    CompositionLocalProvider(LocalTextStyle provides ThemeTypography.button.copy(color = Colors.surface)) {
        Card(
            modifier = modifier
                .clip(shape)
                .clickable(
                    onClick = onClick
                ),
            backgroundColor = color,
            shape = shape,
            contentPadding = contentPaddings,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}