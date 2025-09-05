package com.umain.transport.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("compose.jetbrains").get().get().pluginId)
            apply(libs.findPlugin("compose.compiler").get().get().pluginId)
        }
        val composeDependencies = extensions.getByType<ComposeExtension>().dependencies
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(composeDependencies.runtime)
                        implementation(composeDependencies.foundation)
                        implementation(composeDependencies.material3)
                        implementation(composeDependencies.ui)
                    }
                }
            }
        }
    }
}