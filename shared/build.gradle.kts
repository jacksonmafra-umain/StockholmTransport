plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    androidTarget {
        compilations.all {
        }
    }
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core"))
            api(project(":shared:lines"))
            api(project(":shared:sites"))
            api(project(":shared:departures"))
            api(project(":shared:stoppoints"))
            api(project(":shared:authorities"))
        }
    }
}

android {
    namespace = "com.umain.transport"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}
