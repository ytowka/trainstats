package com.danilkha.trainstats.features.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.danilkha.uikit.theme.LocalPaddings

@Composable
fun EditingSet(){

}

@Composable
fun SetLayout(
    topCorners: Boolean = true,
    bottomCorners: Boolean = true,
    content: @Composable RowScope.() -> Unit,
){
    val radiusBig = LocalPaddings.current.normal
    val radiusSmall = LocalPaddings.current.tiny

    Card(
        shape = RoundedCornerShape(
            topStart = if(topCorners) radiusBig else radiusSmall,
            topEnd = if(topCorners) radiusBig else radiusSmall,
            bottomStart = if(bottomCorners) radiusBig else radiusSmall,
            bottomEnd = if(bottomCorners) radiusBig else radiusSmall
        )
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){

            content()
        }
    }
}

@Composable
fun RestLayout(
    topCorners: Boolean = true,
    bottomCorners: Boolean = true,
    content: @Composable RowScope.() -> Unit,
){
    val radiusBig = LocalPaddings.current.normal
    val radiusSmall = LocalPaddings.current.tiny

    Card(
        shape = RoundedCornerShape(
            topStart = if(topCorners) radiusBig else radiusSmall,
            topEnd = if(topCorners) radiusBig else radiusSmall,
            bottomStart = if(bottomCorners) radiusBig else radiusSmall,
            bottomEnd = if(bottomCorners) radiusBig else radiusSmall
        ),
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){

            content()
        }
    }
}