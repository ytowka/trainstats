package com.danilkha.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.R


object FabDimensions{
    val defaultSize = 56.dp
}

@Composable
fun Fab(
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(id = R.drawable.ic_add),
    color: Color = MaterialTheme.colors.secondary,
    onClick: () -> Unit,
){
    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = color)
            .size(FabDimensions.defaultSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ){
        Icon(
            painter = icon,
            contentDescription = "fab_icon",
            tint = MaterialTheme.colors.onSecondary
        )
    }
}

@Composable
fun FabExtended(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
){
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(50))
            .background(color = color)
            .height(FabDimensions.defaultSize)
            .widthIn(min = FabDimensions.defaultSize)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}