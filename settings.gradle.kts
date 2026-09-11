import kotlin.text.replace

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Training-stats"

includeBuild("build-logic")

fun includeSourceModule(path: String){
    include(path)
    val projectDir = path.replace(':', '/')
    project(path).projectDir = file("sources$projectDir")
    println(projectDir)
}

include(":app")
include(":benchmark")

includeSourceModule(":common-core")
includeSourceModule(":common-ds")
includeSourceModule(":common-date-picker")
includeSourceModule(":common-db")
includeSourceModule(":common-db-api")
includeSourceModule(":shared")

includeSourceModule(":common:alertdialog")
includeSourceModule(":navigation:api")
includeSourceModule(":navigation:impl")
