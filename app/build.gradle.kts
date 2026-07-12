@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

android {
    experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    namespace = "com.danilkha.trainstats"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.danilkha.trainstats"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    ksp {
        arg("room.generateKotlin", "true")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets["androidTest"].assets {
        srcDirs("src/androidTest/assets")
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(project(":common-core"))
    implementation(project(":common-ds"))
    implementation(project(":common-date-picker"))
    implementation(project(":shared"))

    implementation(libs.kotlinx.datetime)

    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.appcompat)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.navigation.compose)

    // koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)

    // room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // tests
    testImplementation(libs.kotest.junit)
    testImplementation(libs.kotest.assert)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.compose.bom.artifact))
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.kotest.assert)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(platform(libs.compose.bom.artifact))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}