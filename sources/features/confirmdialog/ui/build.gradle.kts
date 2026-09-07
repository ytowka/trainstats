plugins {
    id("compose-setup")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common-ds"))
            implementation(project(":features:confirmdialog:entrypoint"))
        }

    }
}