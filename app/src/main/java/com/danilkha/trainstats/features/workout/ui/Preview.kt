package com.danilkha.trainstats.features.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            textStyle = MaterialTheme.typography.h1,
            label = { Text(text = "kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            maxLines = 1,
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
            textStyle = MaterialTheme.typography.h1,
            label = {
                Text(text = "reps")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            maxLines = 1,
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