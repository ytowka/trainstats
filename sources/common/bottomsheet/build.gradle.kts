@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("compose-setup")
}

kotlin {
    androidLibrary {
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            // BackHandler для перехвата системного back в BottomSheetScreen
            implementation(libs.compose.ui.backhandler)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.kotest.junit)
            implementation(libs.kotest.assert)
            implementation(libs.coroutines.test)
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
