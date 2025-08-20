package com.danilkha.trainstats

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec

class SampleTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("given") {
        println("1")
        When("when") {
            println("2")
            Then("then") {
                println("3")
            }
        }
    }
})