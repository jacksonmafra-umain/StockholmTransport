import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

group = "com.umain.transport.buildlogic"

dependencies {
    compileOnly(libs.plugins.kotlinx.serialization.toDep())
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.compose.jetbrains.toDep())
    compileOnly(libs.plugins.multiplatform.toDep())
    compileOnly(libs.plugins.compose.compiler.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatformCore") {
            id = "umain.transport.kmp.core"
            implementationClass = "com.umain.transport.convention.KmpCoreConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "umain.transport.kmp.library"
            implementationClass = "com.umain.transport.convention.KotlinMultiplatformConventionPlugin"
        }

        register("composeMultiplatform") {
            id = "umain.transport.kmp.compose"
            implementationClass = "com.umain.transport.convention.ComposeMultiplatformConventionPlugin"
        }
    }
}