package com.danilkha.trainstats.features.workout.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class Kg(val value: Float)

val Float.kg
    get() = Kg(this)
