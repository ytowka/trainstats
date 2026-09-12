plugins {
    id("compose-setup")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // BackHandler для перехвата системного back в BottomSheetScreen
            implementation(libs.compose.ui.backhandler)
        }
    }
}
