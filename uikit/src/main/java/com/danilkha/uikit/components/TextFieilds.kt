package com.danilkha.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography

@Composable
fun GenericTextFiled(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    textStyle: TextStyle = ThemeTypography.body1.copy(
        color = Colors.text
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    textAlign: TextAlign = textStyle.textAlign,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    cursorBrush: Brush = SolidColor(Colors.text),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    contentStart: @Composable (() -> Unit)? = null,
){
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(
            textAlign = textAlign
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        cursorBrush = cursorBrush,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        decorationBox = { innerTextField ->
            Card(
                contentPadding = contentPadding
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    contentStart?.invoke()
                    Box{
                        if(hint.isNotBlank() && value.isEmpty()){
                            Text(
                                text = hint,
                                style = ThemeTypography.body1,
                                color = Colors.text.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            }
        },
    )
}