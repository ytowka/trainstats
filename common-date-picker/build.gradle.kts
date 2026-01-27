plugins {
    id("compose-setup")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common-core"))
            implementation(project(":common-ds"))

            implementation(libs.compose.material3)
        }
    }
}