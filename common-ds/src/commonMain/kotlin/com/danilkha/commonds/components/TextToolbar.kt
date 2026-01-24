package com.danilkha.commonds.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.theme.ThemeTypography

@Composable
fun TextToolbar(
    title: String,
    onBack: (() -> Unit)? = null,
    endContent: (@Composable RowScope.() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 15.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onBack != null) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    onClick = onBack
                )
            }
            Text(
                text = title,
                style = ThemeTypography.title
            )
            Spacer(Modifier.weight(1f))
            endContent?.invoke(this)
        }
    }
}