package com.umain.transport.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(
    extension: LibraryExtension
) = extension.apply {
    val moduleName = path
        .split(":")
        .drop(2)
        .joinToString(".")
    namespace = if (moduleName.isNotEmpty()) {
        "com.umain.transport.$moduleName"
    } else {
        "com.umain.transport"
    }

    compileSdk = libs
        .findVersion("android-compileSdk")
        .get()
        .requiredVersion
        .toInt()

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}