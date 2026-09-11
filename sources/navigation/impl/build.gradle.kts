
plugins {
    id("compose-setup")
}

kotlin {
    sourceSets {
        commonMain.dependencies {

            implementation(project(":navigation:api"))
            // Navigation 3 (KMP)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        }
    }
}
