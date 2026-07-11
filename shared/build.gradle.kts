@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("compose-setup")
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Room KMP
            implementation(libs.room.runtime)

            // Navigation (JetBrains KMP port)
            implementation(libs.jetbrains.navigation.compose)

            // Koin 4.x
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // SQLite driver for iOS
            implementation(libs.sqlite.bundled)

            // Temporary: javax.inject annotations (until Phase 4 removes Dagger annotations)
            implementation(libs.javax.inject)
        }
        androidMain.dependencies {
            implementation(libs.coroutines.android)
        }
    }
}

dependencies {
    ksp(libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ksp {
    arg("room.generateKotlin", "true")
}
