package com.danilkha.trainstats.features.confirmdialog

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkha.commonds.bottomsheet.BottomSheetScreen
import com.danilkha.commonds.bottomsheet.BottomSheetState
import com.danilkha.commonds.components.Card
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.bottomSheetShape
import com.danilkha.commonds.theme.Colors
import com.danilkha.commonds.theme.ThemeTypography
import com.danilkha.commonds.theme.TrainingStatsTheme
import com.danilkha.trainstats.features.confirmdialog.AlertDialogArgs.CANCEL_ID
import com.danilkha.trainstats.features.confirmdialog.AlertDialogArgs.CONFIRM_ID
import com.danilkha.trainstats.features.confirmdialog.AlertDialogArgs.RESULT_BUTTON_ID
import org.jetbrains.compose.resources.stringResource
import training_stats.shared.generated.resources.Res
import training_stats.shared.generated.resources.*


object AlertDialogArgs {

    const val RESULT_BUTTON_ID = "result"

    const val DISMISS_ID = "dismiss"
    const val CONFIRM_ID = "confirm"
    const val CANCEL_ID= "cancel"
}

@Composable
fun AlertBottomSheetDialog(
    sheetState: BottomSheetState,
    title: String,
    text: String,
){
    BottomSheetScreen(
        state = sheetState
    ) {
        AbstractAlertBottomSheetDialog(
            content = {
                Spacer(modifier = Modifier.size(15.dp))
                AlertBottomSheetText(
                    title = title,
                    text = text
                )
            },
            buttons = {
                GenericButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                    ,
                    onClick = {
                        sheetState.setResult(mapOf(RESULT_BUTTON_ID to CONFIRM_ID))
                        sheetState.hide()
                    },
                    color = Colors.error
                ) {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = Colors.surface,
                        style = ThemeTypography.body1
                    )
                }

                GenericButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                    ,
                    onClick = {
                        sheetState.setResult(mapOf(RESULT_BUTTON_ID to CANCEL_ID))
                        sheetState.hide()
                    },
                    color = Colors.surface
                ) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = Colors.text,
                        style = ThemeTypography.body1
                    )
                }
            }
        )
    }

}

@Composable
fun AbstractAlertBottomSheetDialog(
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
){
    Column(
        modifier = Modifier
            .padding(10.dp)
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
                            text = stringResource(Res.string.delete),
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
                            text = stringResource(Res.string.cancel),
                            color = Colors.text,
                            style = ThemeTypography.body1
                        )
                    }
                }
            )
        }
    }
}
