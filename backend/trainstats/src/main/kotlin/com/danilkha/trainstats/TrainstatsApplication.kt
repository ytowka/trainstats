package com.danilkha.trainstats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TrainstatsApplication

fun main(args: Array<String>) {
	runApplication<TrainstatsApplication>(*args)
}
