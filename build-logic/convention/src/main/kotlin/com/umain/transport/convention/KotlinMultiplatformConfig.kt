package com.umain.transport.convention

import libs
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    jvmToolchain(17)
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm("desktop")
    js {
        binaries.executable()
        browser {
        }
        generateTypeScriptDefinitions()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            isStatic = true
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets.apply {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                api(libs.findLibrary("koin.core").get())
            }
            androidMain {
                dependencies {
                    implementation(libs.findLibrary("koin.android").get())
                    implementation(libs.findLibrary("kotlinx.coroutines.android").get())
                }
                jvmMain.dependencies {
                    implementation(libs.findLibrary("kotlinx.coroutines.swing").get())
                }
            }
        }
    }
}