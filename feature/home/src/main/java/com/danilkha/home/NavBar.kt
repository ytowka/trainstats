package com.danilkha.home

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.uikit.R.*
import com.danilkha.uikit.theme.LocalPaddings
import com.danilkha.uikit.theme.TrainingStatsTheme


object NavigationBarDimensions{
    val height = 56.dp
}



@Composable
fun NavigationBar(
    selectedItem: NavigationItem,
    onItemClicked: (NavigationItem) -> Unit,
){
    val selectedBackgroundColor = MaterialTheme.colors.primary
    val index = NavigationItem.values().indexOf(selectedItem)
    val indexMultiplier by animateFloatAsState(
        targetValue = index.toFloat(), label = "background slide"
    )
    val cornerSize = 4.dp
    val padding = 4.dp

    Row(
        modifier = Modifier
            .height(NavigationBarDimensions.height)
            .background(color = MaterialTheme.colors.surface)
            .drawBehind {
                val width = size.width / NavigationItem.values().size

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
        NavigationItem.values().forEach { item ->
            NavigationBarItem(
                isSelected = item == selectedItem,
                item = item,
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
){
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
        val iconColor = if(isSelected){
            MaterialTheme.colors.onPrimary
        }else MaterialTheme.colors.onSurface

        Icon(
            painter = item.icon,
            contentDescription = null,
            tint = iconColor,
        )
        if(isSelected){
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


private val NavigationItem.icon: Painter
    @Composable
    get() = painterResource(id = when(this){
        NavigationItem.HOME -> drawable.ic_home
        NavigationItem.EXERCISES -> drawable.ic_exercise
        NavigationItem.WORKOUTS -> drawable.ic_list
        NavigationItem.STATS -> drawable.ic_chart
        NavigationItem.PROFILE -> drawable.ic_person
    })

private val NavigationItem.label: String
    @Composable
    get() = stringResource(id = when (this){
        NavigationItem.HOME -> string.navigation_item_home
        NavigationItem.EXERCISES -> string.navigation_item_exercises
        NavigationItem.WORKOUTS -> string.navigation_item_workouts
        NavigationItem.STATS -> string.navigation_item_stats
        NavigationItem.PROFILE -> string.navigation_item_profile
    })

enum class NavigationItem { HOME, EXERCISES, WORKOUTS, STATS, PROFILE }

@Preview
@Composable
fun NavigationBarPreview(){
    var selectedItem by remember { mutableStateOf(NavigationItem.HOME) }

    TrainingStatsTheme {
        NavigationBar(selectedItem =selectedItem, onItemClicked = { selectedItem = it})
    }
}
