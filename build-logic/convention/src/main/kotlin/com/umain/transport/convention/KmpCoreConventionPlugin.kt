package com.umain.transport.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpCoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("multiplatform").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("android-library").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("kotlinx-serialization").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("buildconfig").get().get().pluginId)

        extensions.configure<KotlinMultiplatformExtension> {
            configureKotlinMultiplatform(this)
        }
        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
        }
    }
}