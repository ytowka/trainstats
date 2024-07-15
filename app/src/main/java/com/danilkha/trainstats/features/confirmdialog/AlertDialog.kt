package com.danilkha.trainstats.features.confirmdialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.bottomSheetShape
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography
import com.danilkha.uikit.theme.TrainingStatsTheme


@Composable
fun BaseAlertBottomSheetDialog(
    image: Painter?,
    title: String,
    text: String,
    vararg buttons: AlertBottomSheetButton
){
    AbstractAlertBottomSheetDialog(
        content = {
            if(image != null){
                Image(painter = image, contentDescription = null)
                Spacer(modifier = Modifier.size(15.dp))
            }
            AlertBottomSheetText(
                title = title,
                text = text
            )
        },
        buttons = {
            buttons.forEach { button ->
                GenericButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                    ,
                    onClick = button.onClick,
                    color = button.color
                ) {
                    Text(
                        text = button.text,
                        color = button.textColor,
                        style = ThemeTypography.title
                    )
                }
            }
        }
    )
}

@Composable
fun AbstractAlertBottomSheetDialog(
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
){
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface, shape = bottomSheetShape)
            .padding(10.dp)
            .fillMaxWidth(),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
        Spacer(modifier = Modifier.size(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            content = buttons
        )
    }

}


@Composable
fun AlertBottomSheetText(
    title: String,
    text: String
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Colors.text,
            style = ThemeTypography.subtitle
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = text,
            color = Colors.text.copy(alpha = 0.35f),
            style = ThemeTypography.body1.copy(textAlign = TextAlign.Center)
        )
    }
}

@Immutable
data class AlertBottomSheetButton(
    val color: Color,
    val textColor: Color,
    val icon: Painter? = null,
    val text: String,
    val onClick: () -> Unit
)

@Preview
@Composable
fun BaseAlertBottomSheetDialogDeletePreview(){
    TrainingStatsTheme {
        Box(modifier = Modifier.background(color = Color.Black)){
            AbstractAlertBottomSheetDialog(
                content = {
                    Spacer(modifier = Modifier.size(15.dp))
                    AlertBottomSheetText(
                        title = "Delete this product?",
                        text = "The product will be deleted from this day"
                    )
                },
                buttons = {
                    GenericButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                        ,
                        onClick = {  },
                        color = Colors.error
                    ) {
                        Text(
                            text = stringResource(id = com.danilkha.trainstats.R.string.delete),
                            color = Colors.surface,
                            style = ThemeTypography.body1
                        )
                    }

                    GenericButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                        ,
                        onClick = {  },
                        color = Colors.surface
                    ) {
                        Text(
                            text = stringResource(id = com.danilkha.trainstats.R.string.cancel),
                            color = Colors.text,
                            style = ThemeTypography.body1
                        )
                    }
                }
            )
        }
    }
}

