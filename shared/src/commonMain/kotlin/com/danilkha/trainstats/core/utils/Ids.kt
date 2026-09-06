package com.danilkha.trainstats.core.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun generateId(): String = Uuid.random().toString()
