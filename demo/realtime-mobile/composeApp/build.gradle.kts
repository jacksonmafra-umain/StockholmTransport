import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName("com.umain.transport.realtime.config")

    buildConfigField("String", "SERVER_HOST_URL", "\"${project.property("serverHostURL")}\"")
    buildConfigField("String", "SERVER_HOST", "\"${project.property("serverHost")}\"")
    buildConfigField("Int", "SERVER_PORT", (project.property("serverPort") as String).toInt())
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())

    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            implementation(libs.kotlinx.coroutines.core)

            // Stockholm Transport library — owns Ktor/Serialization/Datetime
            // transitively, plus the realtime feature's TripRepository,
            // TripViewModel, TripSelectionViewModel, RealtimeConfig, and the
            // platform HttpClient `actual`s. After Option C the demo only
            // depends on the SDK contract; no networking code lives here.
            implementation(libs.stockholm.transport)

            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }

        iosMain.dependencies {
            // Realtime networking is provided by the library's iOS source
            // set (Ktor Darwin engine ships transitively from
            // :stockholm-transport).
        }
    }
}

android {
    namespace = "com.umain.transport.realtime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "com.umain.transport.realtime.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
}
