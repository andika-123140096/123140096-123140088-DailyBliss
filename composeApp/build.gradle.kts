import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

// Load local.properties for API keys
val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // DataStore + Okio
            implementation(libs.datastore.preferences)
            implementation(libs.okio)

            // Lifecycle & ViewModel
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)

            // Navigation
            implementation(libs.navigation.compose)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Peekaboo
            implementation(libs.peekaboo.image.picker)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.ktor.client.mock)
            implementation(libs.mockk)
            implementation(libs.koin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(compose.preview)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.junit)
                implementation(libs.espresso.core)
                implementation(libs.androidx.compose.ui.test.junit4)
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
    }
}

android {
    namespace = "com.dailybliss.app"
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.dailybliss.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API key and model from local.properties
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"",
        )
        buildConfigField(
            "String",
            "GEMINI_MODEL_NAME",
            "\"${localProperties.getProperty("GEMINI_MODEL_NAME", "gemini-1.5-flash")}\"",
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Support 16KB devices for 3rd party libs like Coil
            useLegacyPackaging = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

sqldelight {
    databases {
        create("BlissDatabase") {
            packageName.set("com.dailybliss.app.data.local")
        }
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.dailybliss.app.presentation.*",
                    "com.dailybliss.app.AppKt*",
                    "com.dailybliss.app.MainActivity*",
                    "com.dailybliss.app.DailyBlissApplication*",
                    "com.dailybliss.app.BuildConfig*",
                    "com.dailybliss.app.core.di.*",
                    "com.dailybliss.app.core.network.*",
                    "*ComposableSingletons*",
                    "com.dailybliss.app.data.repository.*",
                    "com.dailybliss.app.data.remote.*",
                    "com.dailybliss.app.data.local.*",
                    "com.dailybliss.app.core.util.*",
                    "dailybliss.composeapp.generated.*",
                    "com.dailybliss.app.domain.repository.AIRepository${'$'}DefaultImpls",
                )
            }
        }
    }
}
