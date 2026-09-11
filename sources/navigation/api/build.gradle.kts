
plugins {
    id("multiplatform-library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {

            // Navigation 3 (KMP)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.serialization.core)

            // staticCompositionLocalOf для LocalNavigator
            implementation(libs.compose.runtime)
        }
    }
}
