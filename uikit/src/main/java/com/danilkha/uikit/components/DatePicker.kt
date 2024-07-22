package com.danilkha.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.R
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.TrainingStatsTheme
import korlibs.time.Date
import korlibs.time.DateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit,
) {

    val today = DateTime.nowLocal().local
    val state = rememberDatePickerState(today.unixMillisLong)

    val colors = DatePickerDefaults.colors(
        selectedDayContentColor = Colors.textInverse,
        selectedDayContainerColor = Colors.primary,
        todayDateBorderColor = Colors.primary,
        todayContentColor = Colors.text,
        yearContentColor = Colors.text,
        disabledYearContentColor = Colors.text.copy(alpha = 0.5f),
        selectedYearContentColor = Colors.textInverse,
        currentYearContentColor = Colors.text,
        selectedYearContainerColor = Colors.primary,
        disabledSelectedYearContentColor = Colors.primary,
        disabledSelectedYearContainerColor = Colors.primary,
        containerColor = Colors.surface,

    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        colors = colors
    ) {
        Column {
            DatePicker(
                state = state,
                showModeToggle = true,
                colors = colors
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GenericButton(
                    modifier = Modifier
                        .width(100.dp),
                    onClick = onDismiss,
                    color = Colors.primaryVariant
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = Colors.textInverse
                    )
                }
                GenericButton(
                    modifier = Modifier
                        .width(100.dp),
                    color = Colors.primaryVariant,
                    onClick = {
                        state.selectedDateMillis?.let {
                            val date = DateTime(it).date
                            onDateSelected(date)
                        }
                    }) {
                    Text(
                        text = stringResource(id = R.string.select),
                        color = Colors.textInverse
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun DateSelectorPreview(){
    TrainingStatsTheme {
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Colors.background),
            contentAlignment = Alignment.Center
        ){
            DateSelector(
                onDismiss = {},
                onDateSelected = {}
            )
        }
    }
}