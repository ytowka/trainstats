plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
}

compose.resources {
    publicResClass = true
}

kotlin {
    androidLibrary {
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        namespace = "com.danilkha.commonds"
        compileSdk = 36
        minSdk = 26
    }

    val xcfName = "common-coreKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiTooling.kmp)
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":common-core"))

            implementation(libs.compose.runtime.kmp)
            implementation(libs.compose.foundation.kmp)
            implementation(libs.compose.ui.kmp)
            implementation(libs.compose.components.resources.kmp)
            implementation(libs.compose.uiToolingPreview.kmp)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.compose.material.kmp)

            implementation(libs.compose.components.resources.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }

}