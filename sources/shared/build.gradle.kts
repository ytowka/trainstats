@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("compose-setup")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common-ds"))
            implementation(project(":common-date-picker"))
            implementation(project(":common:alertdialog"))
            implementation(project(":common-db"))
            implementation(libs.room.runtime) // RoomDatabase.Builder used in DataModule
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Navigation (JetBrains KMP port)
            implementation(libs.jetbrains.navigation.compose)

            // Koin 4.x
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.coroutines.android)
        }
    }
}
