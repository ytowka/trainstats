import gradle.kotlin.dsl.accessors._2035c3f0e1508481f6a78e92762cd0f2.androidLibrary

plugins {
    id("multiplatform-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

private fun library(alias: String) = libs.findLibrary(alias).get()

compose.resources {
    publicResClass = true
}

kotlin {
    androidLibrary {
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }
    sourceSets {
        androidMain.dependencies {
            implementation(library("compose.ui.tooling"))
            implementation(library("compose.ui.tooling.preview"))
        }
        commonMain.dependencies {
            implementation(project(":common-core"))

            implementation(library("compose.runtime"))
            implementation(library("compose.foundation"))
            implementation(library("compose.ui"))
            implementation(library("compose.components.resources"))
            implementation(library("compose.ui.tooling.preview"))
            implementation(library("compose.material"))
            implementation(library("compose.components.resources"))
            implementation(library("compose.material.icons.extended"))
            implementation(library("jetbrains.lifecycle.viewmodel.compose"))
            implementation(library("jetbrains.lifecycle.runtime.compose"))
        }
    }
}