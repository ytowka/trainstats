package com.danilkha.trainstats.features.settings.workoutimport.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.viewmodel.LaunchCollectEffects
import com.danilkha.trainstats.core.viewmodel.getCurrentViewModel
import com.danilkha.commonds.components.GenericButton
import com.danilkha.commonds.components.GenericTextFiled
import com.danilkha.commonds.components.TextToolbar
import com.danilkha.commonds.theme.Colors

@Composable
fun ImportScreenRoute(
    viewModel: ImportViewModel = getCurrentViewModel { it.profileViewModel },
    onBack: () -> Unit,
){
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    viewModel.LaunchCollectEffects { event ->
        when(event){
            is ImportSideEffect.ImportSuccess -> {
                Toast.makeText(context, "import ok exercises: ${event.exercises}, workouts: ${event.workouts}", Toast.LENGTH_SHORT).show()
            }

            ImportSideEffect.Error -> {
                Toast.makeText(context, "error parsing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ImportScreen(
        state = state,
        eventConsumer = viewModel::processEvent,
        onBack = onBack
    )
}

@Composable
fun ImportScreen(
    state: ImportState,
    eventConsumer: (ImportEvent) -> Unit,
    onBack: () -> Unit,
) {
    val errorColor = Colors.error

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextToolbar(
            title = stringResource(id = R.string.import_workout),
            onBack = onBack,
        )

        val textScrollState = rememberScrollState()

        GenericTextFiled(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(state = textScrollState, reverseScrolling = true)
                .fillMaxWidth()
                .weight(1f),
            value = state.exportText,
            onValueChange = { eventConsumer(ImportEvent.ChangeImportText(it)) },
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    GenericButton(
                        modifier = Modifier.weight(1f),
                        onClick = { eventConsumer(ImportEvent.ImportFromText) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.to_import)
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    FilePickerButton { eventConsumer(ImportEvent.ImportFromFile(it)) }
                }
            }
        }
    }
}


@Composable
private fun RowScope.FilePickerButton(
    onFilePicked: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            onFilePicked(uri)
        }
    )

    GenericButton(
        modifier = Modifier.weight(1f),
        onClick = { launcher.launch(arrayOf("text/plain")) }
    ) {
        Text(
            text = stringResource(id = R.string.from_file)
        )
    }
}