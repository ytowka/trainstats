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

include(":app")
include(":uikit")
include(":common-core")
include(":common-ds")
include(":common-date-picker")
