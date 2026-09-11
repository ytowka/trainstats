
plugins {
    id("multiplatform-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {

            // Navigation 3 (KMP)
            implementation(libs.androidx.navigation3.runtime)
        }
    }
}
