package com.danilkha.trainstats.features.profile.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.LaunchCollectEffects
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.GenericButton
import com.danilkha.uikit.components.GenericTextFiled
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = getCurrentViewModel { it.profileViewModel }
){
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    viewModel.LaunchCollectEffects{ event ->
        when(event){
            is ProfileSingleEvent.ImportSuccess -> {
                Toast.makeText(context, "import ok exercises: ${event.exercises}, workouts: ${event.workouts}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    val errorColor = Colors.error

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 15.dp
            )
        ) {
            Text(
                text = stringResource(id = R.string.import_workout),
                style = ThemeTypography.title
            )
        }

        var textHeight by remember { mutableIntStateOf(0) }
        val textScrollState = rememberScrollState()

        GenericTextFiled(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(state = textScrollState, reverseScrolling = true)
                .fillMaxWidth()
                .weight(1f),
            value = state.exportText,
            onValueChange = viewModel::onExportTextChange,
            onTextLayout = {
                textHeight = it.size.height
            },
            visualTransformation = {
                TransformedText(
                    text = AnnotatedString(
                        text = state.exportText, spanStyles = listOf(
                            AnnotatedString.Range(
                                item = SpanStyle(background = errorColor),
                                start = state.errorLine?.start ?: 0,
                                end = state.errorLine?.end ?: 0
                            )
                        )
                    ),
                    offsetMapping = OffsetMapping.Identity
                )
            },
            singleLine = false,
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if(state.isLoading){
                CircularProgressIndicator()
            }else{
                GenericButton(
                    onClick = viewModel::onExportClicked
                ) {
                    Text(
                        text = stringResource(id = R.string.to_import)
                    )
                }
            }
        }
    }
}