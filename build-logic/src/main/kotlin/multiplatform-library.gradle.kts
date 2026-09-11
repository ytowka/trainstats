import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.kotlin
interface ModuleSetupExtension {
    val name: Property<String>
}

val extension = extensions.create<ModuleSetupExtension>("moduleSetup")
extension.name.convention(project.name.replace("-", ""))


plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    val moduleName = extension.name.get()

    androidLibrary {
        namespace = "com.danilkha.$moduleName"
        compileSdk = 36
        minSdk = 26
    }

    val xcfName = "${moduleName}Kit"

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }
}