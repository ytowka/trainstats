package com.danilkha.commoncore.utils

import java.text.DecimalFormat

private val floatFormat1 = DecimalFormat("###########.#")
private val floatFormat2 = DecimalFormat("###########.##")

actual fun Float.format1(): String = floatFormat1.format(this)
actual fun Float.format2(): String = floatFormat2.format(this)