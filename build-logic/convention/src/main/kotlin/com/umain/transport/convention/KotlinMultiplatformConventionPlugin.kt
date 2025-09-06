package com.umain.transport.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("umain.transport.kmp.core")

        dependencies {
            add("commonMainImplementation", project(":shared:core"))
        }
    }
}