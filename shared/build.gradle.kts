import org.jetbrains.kotlin.gradle.targets.js.npm.PackageJson

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.npm.publish)
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilations.all {
        }
    }
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    js(IR) {
        useEsModules()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
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
