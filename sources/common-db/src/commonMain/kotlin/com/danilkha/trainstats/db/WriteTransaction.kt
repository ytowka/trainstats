package com.danilkha.trainstats.db

import androidx.room.Transactor.SQLiteTransactionType
import androidx.room.useWriterConnection

suspend fun <T> TrainStatsDb.writeTransaction(block: suspend () -> T): T =
    useWriterConnection { connection ->
        connection.withTransaction(SQLiteTransactionType.IMMEDIATE) { block() }
    }
