package com.danilkha.trainstats.features.workout.ui.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkha.trainstats.R
import com.danilkha.trainstats.core.utils.floatPrecision
import com.danilkha.trainstats.core.utils.format2
import com.danilkha.trainstats.core.utils.trimDots
import com.danilkha.trainstats.core.utils.trimFirstZeros
import com.danilkha.trainstats.features.workout.domain.model.Kg
import com.danilkha.trainstats.features.workout.ui.ExerciseSetSlot
import com.danilkha.trainstats.features.workout.ui.RepetitionsModel
import com.danilkha.trainstats.features.workout.ui.SET_DELETE_DELAY
import com.danilkha.trainstats.features.workout.ui.Side
import com.danilkha.uikit.components.Card
import com.danilkha.uikit.components.DragAndDropColumn
import com.danilkha.uikit.components.DragDispatcher
import com.danilkha.uikit.components.GenericTextFiled
import com.danilkha.uikit.components.Icon
import com.danilkha.uikit.theme.Colors
import com.danilkha.uikit.theme.ThemeTypography
import com.danilkha.uikit.theme.TrainingStatsTheme
import korlibs.time.DateTime

@Composable
fun ExerciseGroupCard(
    title: String,
    expanded: Boolean,
    sets: List<ExerciseSetSlot>,
    deleted: Set<Long>,
    separated: Boolean,
    hasWeight: Boolean,

    onWeightChange: (index: Int, Float) -> Unit,
    onRepsChange: (index: Int, Side?, value: Float) -> Unit,
    onDelete: (index: Int) -> Unit,
    onReturnDeleted: (index: Int) -> Unit,
    onSetMoved: (from: Int, to: Int) -> Unit,
    onDeleteGroup: () -> Unit,

    onExpandClick: () -> Unit,
    onDragStart: () -> Unit ,
    onDragEnd: () -> Unit,
    onVerticalDrag: (dragAmount: Float) -> Unit
) {
    Card {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
        ) {
            DragThumb(
                onDragStart = onDragStart, onDragEnd = onDragEnd, onVerticalDrag = onVerticalDrag
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = title,
                style = ThemeTypography.title
            )
            Spacer(modifier = Modifier.size(10.dp))
            if(sets.size == 1){
                Icon(
                    imageVector = Icons.Default.Clear,
                    onClick = onDeleteGroup
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            val rotation = animateFloatAsState(targetValue = if (expanded) 180f else 0f)
            Icon(
                modifier = Modifier.rotate(rotation.value),
                imageVector = Icons.Default.KeyboardArrowDown,
                onClick = onExpandClick
            )
        }
        AnimatedVisibility(visible = expanded) {
            Spacer(modifier = Modifier.size(10.dp))
            Card(
                modifier = Modifier.clip(RectangleShape).animateContentSize(),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp),
                backgroundColor = Colors.background
            ){
                val dragDispatcher = remember { DragDispatcher() }
                DragAndDropColumn(
                    items = sets,
                    onItemMoved = onSetMoved,
                    dragDispatcher = dragDispatcher,
                    keyProvider = { index, it -> it.tempId }
                ) { index, item ->
                    val updatedIndex by rememberUpdatedState(newValue = index)
                    val asModel = item as? ExerciseSetSlot.ExerciseSetModel
                    ExerciseSet(
                        reps = asModel?.reps ?: when(separated){
                            true -> RepetitionsModel.Double(null, null)
                            false -> RepetitionsModel.Single(null)
                        },
                        hasWeight = hasWeight,
                        isStub = item is ExerciseSetSlot.Stub,
                        weight = asModel?.weight,
                        deleted = asModel?.tempId in deleted,
                        onWeightChange = { onWeightChange(index, it) },
                        onRepsChange = { side, fl -> onRepsChange(index, side, fl) },
                        onDelete = { onDelete(index) },
                        onReturnDeleted = { onReturnDeleted(index) },
                        onDragStart = { dragDispatcher.onDragStart(updatedIndex) },
                        onDragEnd = { dragDispatcher.onDragEnd() },
                        onVerticalDrag = { dragDispatcher.onDrag(it) }
                    )
                }
            }
        }

    }
}

@Composable
fun DragThumb(
    onDragStart: () -> Unit = { },
    onDragEnd: () -> Unit = { },
    onVerticalDrag: (dragAmount: Float) -> Unit
){
    Image(
        modifier = Modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {onDragStart()},
                    onVerticalDrag = { change, dragAmount -> onVerticalDrag(dragAmount) },
                    onDragEnd = onDragEnd,
                )
            },
        imageVector = Icons.Default.DragHandle, contentDescription = null,
        colorFilter = ColorFilter.tint(Colors.text),
        alpha = 0.3f
    )
}


private const val MAX_FIELD_LENGTH = 6
private const val MAX_REPS_FIELD_LENGTH = 3
private val FIELD_WIDTH = 90.dp
private val REPS_FIELD_WIDTH = 50.dp

private val textFieldStyle = TextStyle(
    textAlign = TextAlign.Center,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
)

@Composable
fun ExerciseSet(
    reps: RepetitionsModel,
    hasWeight: Boolean,
    weight: Kg?,
    deleted: Boolean,
    isStub: Boolean,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Side?, Float) -> Unit,
    onDelete: () -> Unit,
    onReturnDeleted: () -> Unit,
    onDragStart: () -> Unit = { },
    onDragEnd: () -> Unit = { },
    onVerticalDrag: (dragAmount: Float) -> Unit
){
    val defaultColor = Colors.background
    val deletedColor = Colors.error
    val backgroundColor = remember {
        Animatable(defaultColor)
    }

    LaunchedEffect(key1 = deleted) {
        if(deleted){
            backgroundColor.animateTo(deletedColor, animationSpec = tween(SET_DELETE_DELAY.toInt()))
        }else{
            backgroundColor.animateTo(defaultColor,)
        }
    }
    
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor.value,
                shape = RoundedCornerShape(10.dp)
            )
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(!isStub){
            DragThumb(
                onDragStart = onDragStart,
                onVerticalDrag = onVerticalDrag,
                onDragEnd = onDragEnd
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(hasWeight){
                var weightText by remember { mutableStateOf(weight?.value?.format2() ?: "") }

                GenericTextFiled(
                    modifier = Modifier.width(FIELD_WIDTH),
                    value = weightText,
                    textStyle = textFieldStyle,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    onValueChange = {
                        if(it.length <= MAX_FIELD_LENGTH){
                            weightText = it
                                .trimFirstZeros()
                                .trimDots()
                                .floatPrecision(2)
                            val value = weightText.toFloatOrNull()
                            value?.let(onWeightChange)
                        }
                    }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(id = R.string.kg),
                    style = ThemeTypography.body1.copy(fontWeight = FontWeight.Normal)
                )
                Spacer(modifier = Modifier.size(15.dp))
            }
            when(reps){
                is RepetitionsModel.Double -> {
                    var repsTextL by remember { mutableStateOf(reps.left?.format2() ?: "") }
                    var repsTextR by remember { mutableStateOf(reps.right?.format2() ?: "") }

                    GenericTextFiled(
                        modifier = Modifier.width(REPS_FIELD_WIDTH),
                        value = repsTextL,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        textStyle = textFieldStyle,
                        onValueChange = {
                            if(it.length <= MAX_REPS_FIELD_LENGTH){
                                repsTextL = it
                                    .trimFirstZeros()
                                    .trimDots()
                                    .floatPrecision(2)
                                val value = repsTextL.toFloatOrNull()
                                value?.let{ onRepsChange(Side.Left, value) }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    GenericTextFiled(
                        modifier = Modifier.width(REPS_FIELD_WIDTH),
                        value = repsTextR,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        textStyle = textFieldStyle,
                        onValueChange = {
                            if(it.length <= MAX_REPS_FIELD_LENGTH){
                                repsTextR = it
                                    .trimFirstZeros()
                                    .trimDots()
                                    .floatPrecision(2)
                                val value = repsTextR.toFloatOrNull()
                                value?.let{ onRepsChange(Side.Right, value) }
                            }
                        }
                    )
                }
                is RepetitionsModel.Single -> {
                    var repsText by remember { mutableStateOf(reps.reps?.format2() ?: "") }

                    GenericTextFiled(
                        modifier = Modifier.width(REPS_FIELD_WIDTH),
                        value = repsText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        textStyle = textFieldStyle,
                        onValueChange = {
                            if(it.length <= MAX_REPS_FIELD_LENGTH){
                                repsText = it
                                    .trimFirstZeros()
                                    .trimDots()
                                    .floatPrecision(2)
                                val value = repsText.toFloatOrNull()
                                value?.let{ onRepsChange(null, value) }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = stringResource(id = R.string.reps),
                style = ThemeTypography.body1.copy(fontWeight = FontWeight.Normal)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        if(deleted){
            Icon(
                imageVector = Icons.AutoMirrored.Default.Undo,
                alpha = 0.5f,
                onClick = onReturnDeleted
            )
        }else if(!isStub){
            Icon(
                imageVector = Icons.Default.Clear,
                alpha = 0.5f,
                onClick = onDelete
            )
        }
    }
}

@Composable
@Preview
private fun ExerciseSetPreview(){
    TrainingStatsTheme {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(color = Colors.background)
            .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            val sets = listOf(
                ExerciseSetSlot.ExerciseSetModel(
                    tempId = 0,
                    dateTime = DateTime.now(),
                    reps = RepetitionsModel.Single(10f),
                    weight = Kg(10f)
                ),
                ExerciseSetSlot.ExerciseSetModel(
                    tempId = 1,
                    dateTime = DateTime.now(),
                    reps = RepetitionsModel.Single(9f),
                    weight = Kg(20f)
                ),
                ExerciseSetSlot.ExerciseSetModel(
                    tempId = 2,
                    dateTime = DateTime.now(),
                    reps = RepetitionsModel.Single(8f),
                    weight = Kg(30f)
                )
            )
            ExerciseGroupCard(
                title = "Жим лежа",
                expanded = true,
                separated = false,
                hasWeight = true,
                sets = sets,
                deleted = emptySet(),
                onWeightChange = { i: Int, fl: Float -> },
                onRepsChange = { i: Int, side: Side?, fl: Float -> },
                onDelete = {},
                onReturnDeleted = {},
                onExpandClick = {},
                onDragStart = {},
                onDragEnd = {},
                onVerticalDrag = {},
                onSetMoved = { from, to ->  },
                onDeleteGroup = {},
            )

            ExerciseGroupCard(
                title = "подтягивания",
                expanded = false,
                separated = false,
                hasWeight = true,
                sets = emptyList(),
                deleted = emptySet(),
                onWeightChange = { i: Int, fl: Float -> },
                onRepsChange = { i: Int, side: Side?, fl: Float -> },
                onDelete = {},
                onReturnDeleted = {},
                onExpandClick = {},
                onDragStart = {},
                onDragEnd = {},
                onVerticalDrag = {},
                onSetMoved = { from, to ->  },
                onDeleteGroup = {},
            )

            val sets2 = listOf(
                ExerciseSetSlot.ExerciseSetModel(
                    tempId = 0,
                    dateTime = DateTime.now(),
                    reps = RepetitionsModel.Double(10f, 10f),
                    weight = Kg(10f)
                ),
                ExerciseSetSlot.ExerciseSetModel(
                    tempId = 1,
                    dateTime = DateTime.now(),
                    reps = RepetitionsModel.Double(10f, 10f),
                    weight = Kg(20f)
                ),
            )

            ExerciseGroupCard(
                title = "Жим лежа",
                expanded = true,
                separated = true,
                sets = sets2,
                deleted = emptySet(),
                onWeightChange = { i: Int, fl: Float -> },
                onRepsChange = { i: Int, side: Side?, fl: Float -> },
                onDelete = {},
                onReturnDeleted = {},
                onExpandClick = {},
                onDragStart = {},
                onDragEnd = {},
                onVerticalDrag = {},
                onSetMoved = { from, to ->  },
                hasWeight = true,
                onDeleteGroup = {},
                )
        }

    }
}