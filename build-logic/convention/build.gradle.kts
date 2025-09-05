plugins {
    `kotlin-dsl`
}

kotlin{
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
        register("kotlinMultiplatform"){
            id = "com.umain.transport.kotlinMultiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform"){
            id = "com.umain.transport.composeMultiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
    }
}