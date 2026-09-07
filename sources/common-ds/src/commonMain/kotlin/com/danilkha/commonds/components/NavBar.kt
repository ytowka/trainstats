package com.danilkha.commonds.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.commonds.theme.LocalPaddings

object NavigationBarDimensions {
    val height = 56.dp
}

@Composable
fun <T> NavigationBar(
    items: List<T>,
    selectedItem: T,
    onItemClicked: (T) -> Unit,
    itemFactory: @Composable (T) -> NavigationItem,
) {
    val selectedBackgroundColor = MaterialTheme.colors.primary
    val index = items.indexOf(selectedItem)
    val indexMultiplier by animateFloatAsState(
        targetValue = index.toFloat(), label = "background slide"
    )
    val cornerSize = 4.dp
    val padding = 4.dp

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .height(NavigationBarDimensions.height)
            .drawBehind {
                val width = size.width / items.size

                drawRoundRect(
                    color = selectedBackgroundColor,
                    topLeft = Offset(
                        x = width * indexMultiplier + padding.toPx(),
                        y = padding.toPx()
                    ),
                    size = Size(
                        width = width - padding.toPx() * 2,
                        size.height - padding.toPx() * 2,
                    ),
                    cornerRadius = CornerRadius(cornerSize.toPx(), cornerSize.toPx())
                )
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                isSelected = item == selectedItem,
                item = itemFactory(item),
                onClick = { onItemClicked(item) }
            )
        }
    }
}

@Composable
fun RowScope.NavigationBarItem(
    isSelected: Boolean,
    item: NavigationItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(LocalPaddings.current.tiny)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
            .semantics {
                contentDescription = "navigation_item_$item"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconColor = if (isSelected) {
            MaterialTheme.colors.onPrimary
        } else MaterialTheme.colors.onSurface

        Icon(
            painter = item.icon,
            contentDescription = null,
            tint = iconColor,
        )
        if (isSelected) {
            Text(
                text = item.label,
                color = iconColor,
                style = MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                )
            )
        }
    }
}

@Immutable
data class NavigationItem(
    val label: String,
    val icon: Painter
)
