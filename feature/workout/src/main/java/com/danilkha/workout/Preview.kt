package com.danilkha.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkha.uikit.theme.CustomColors
import com.danilkha.uikit.theme.TrainingStatsTheme
import kotlin.random.Random

@Composable
@Preview
fun Preview() {
    TrainingStatsTheme{
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues()
            ){

            }
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditingSet(bottomCorners = false, enabled = false)
                EditingSet(topCorners = false, bottomCorners = false,  enabled = false)
                EditingSet(topCorners = false, bottomCorners = false, enabled = false)
                EditingSet(topCorners = false, bottomCorners = false )
                EditingSet(topCorners = false, bottomCorners = false )
                EditingSet(topCorners = false)
            }
        }
    }
}

@Composable
fun EditingSet(
    topCorners: Boolean = true,
    bottomCorners: Boolean = true,
    enabled: Boolean = true,
){
    SetLayout(
        topCorners = topCorners,
        bottomCorners = bottomCorners,
    ) {
        var weight by remember {
            mutableStateOf(Random.nextInt(10,20).toString())
        }

        var reps by remember {
            mutableStateOf(Random.nextInt(10,20).toString())
        }

        TextField(
            modifier = Modifier.width(100.dp),
            value = weight,
            onValueChange = { if(it.length < 5){
                weight = it
            } },
            enabled = enabled,
            textStyle = MaterialTheme.typography.titleLarge,
            label = {Text(text = "kg")},

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            maxLines = 1,
            colors = CustomColors.noIndicator
        )

        TextField(
            modifier = Modifier.width(100.dp),
            value = reps,
            enabled = enabled,
            onValueChange = {
                if(it.length < 5){
                    reps = it
                }
            },
            textStyle = MaterialTheme.typography.titleLarge,
            label = {
                Text(text = "reps")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            maxLines = 1,
            colors = CustomColors.noIndicator
        )
    }
}

@Composable
fun Set(){

}

@Composable
fun EditingRest(){

}

@Composable
fun Rest(){

}