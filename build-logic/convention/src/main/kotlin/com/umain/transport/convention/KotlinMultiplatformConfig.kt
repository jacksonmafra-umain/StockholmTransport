package com.umain.transport.convention

import libs
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) =
    extension.apply {
        jvmToolchain(17)
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
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

        applyDefaultHierarchyTemplate()
        sourceSets.apply {
            commonMain {
                dependencies {
                    implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                    api(libs.findLibrary("koin.core").get())
                    implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                    implementation(libs.findLibrary("koin.core").get())
                    implementation(libs.findLibrary("kotlinx.datetime").get())
                    implementation(libs.findLibrary("ktor.client.core").get())
                    implementation(libs.findLibrary("ktor.client.content.negotiation").get())
                    implementation(libs.findLibrary("ktor.serialization.kotlinx.json").get())

                    implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                    implementation(libs.findLibrary("kotlinx.coroutines.test").get())
                    implementation(libs.findLibrary("kotlinx.serialization.json").get())
                    implementation(libs.findLibrary("kotlinx.datetime").get())
                    implementation(libs.findLibrary("kermit").get())
                    implementation(libs.findLibrary("ktor.client.core").get())
                    implementation(libs.findLibrary("ktor.client.content.negotiation").get())
                    implementation(libs.findLibrary("ktor.client.serialization").get())
                    implementation(libs.findLibrary("ktor.serialization.json").get())
                    implementation(libs.findLibrary("ktor.client.logging").get())
                }
                androidMain {
                    dependencies {
                        implementation(libs.findLibrary("koin.android").get())
                        implementation(libs.findLibrary("kotlinx.coroutines.android").get())
                    }
                }
                jvmMain.dependencies {
                    implementation(libs.findLibrary("kotlinx.coroutines.swing").get())
                }
            }
        }
    }
