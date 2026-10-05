@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("compose-setup")
}

compose.resources {
    packageOfResClass = "training_stats.shared.generated.resources"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            implementation(project(":common-ds"))
            implementation(project(":common-date-picker"))
            implementation(project(":common:alertdialog"))
            implementation(project(":common:bottomsheet"))
            implementation(project(":navigation:api"))
            implementation(project(":navigation:impl"))
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
