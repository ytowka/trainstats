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

rootProject.name = "Training stats"
include(":app")
include(":feature:workout")
include(":core:domain")
include(":core:framework")
include(":data:workouts")
include(":core:uikit")
include(":core:androidframework")
