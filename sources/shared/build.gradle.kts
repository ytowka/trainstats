@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("multiplatform-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common-core"))
            implementation(project(":common-db"))
            implementation(libs.room.runtime) // RoomDatabase.Builder used in DataModule
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Koin 4.x
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.coroutines.android)
        }
    }
}
