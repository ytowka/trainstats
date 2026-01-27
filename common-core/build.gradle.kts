plugins {
    id("multiplatform-library")
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
                api(libs.kotlinx.datetime)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.napier)
            }
        }
    }
}