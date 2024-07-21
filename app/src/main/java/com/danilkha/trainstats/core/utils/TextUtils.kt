package com.danilkha.trainstats.core.utils

import java.text.DecimalFormat

private val floatFormat1 = DecimalFormat("###########.#")
private val floatFormat2 = DecimalFormat("###########.##")

fun Float.formatExact(scale: Int = 2) = "%.${scale}f".format(this)

fun Float.format1() = floatFormat1.format(this)
fun Float.format2() = floatFormat2.format(this)

fun CharSequence.trimFirstZeros(): String = buildString {
    var firstZeros = true
    this@trimFirstZeros.forEachIndexed { index, c ->
        if(c != '0' || !firstZeros || index == 0){
            if(c != '0'){
                firstZeros = false
            }
            append(c)

        }
    }
}

fun CharSequence.floatPrecision(maxDigitsAfterDot: Int = 1): String = buildString {
    var hasDot = false
    var digitsAfterDot = 0
    this@floatPrecision.forEachIndexed { index, c ->
        if(!hasDot || digitsAfterDot < maxDigitsAfterDot){
            append(c)
            if(hasDot){
                digitsAfterDot++
            }
            if(c == '.' || c == ','){
                hasDot = true
            }
        }
    }
}

fun CharSequence.trimDots(): String = buildString {
    var hasDot = false
    this@trimDots.forEachIndexed { index, c ->
        if(c != ',' && c != '.' || !hasDot){
            append(c)
            if(c == '.' || c == ','){
                hasDot = true
            }
        }
    }
}