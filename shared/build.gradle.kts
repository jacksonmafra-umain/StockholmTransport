plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildConfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {

        }
    }
}

android {
    namespace = "com.umain.transport"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
buildConfig {
    packageName("com.umain.transport.config")

    buildConfigField("String", "SERVER_HOST_URL", "\"${project.property("serverHostURL")}\"")
    buildConfigField("String", "SERVER_HOST", "\"${project.property("serverHost")}\"")
    buildConfigField("Int", "SERVER_PORT", (project.property("serverPort") as String).toInt())
}