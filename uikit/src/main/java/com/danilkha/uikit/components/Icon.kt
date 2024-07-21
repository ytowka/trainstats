package com.danilkha.uikit.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.vector.ImageVector
import com.danilkha.uikit.theme.Colors

@Composable
fun Icon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    color: Color = Colors.text,
    alpha: Float = DefaultAlpha,
    onClick: (() -> Unit)? = null,
){
    Image(
        modifier = modifier
            .then(if(onClick != null) Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick
            ) else Modifier),
        imageVector = imageVector,
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(color),
        alpha = alpha
    )
}