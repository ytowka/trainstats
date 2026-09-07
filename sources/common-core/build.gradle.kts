plugins {
    id("multiplatform-library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.lint)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.coroutines.core)
                implementation(libs.compose.runtime)
                api(libs.kotlinx.datetime)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.napier)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.compose.ui)
            }
        }
    }
}
