@file:OptIn(ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val libDeveloperOrg: String by project
val libMavenPublish: String by project
val libDescription: String by project
val libSiteUrl: String by project
val libGitUrl: String by project
val libBaseVersion: String by project
val jvmToolchainVersion: String by project
val developerId: String by project
val developerName: String by project
val developerEmail: String by project

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.github.gmazzo.buildconfig")
    id("maven-publish")
    id("io.insert-koin.compiler.plugin")
}

group = libDeveloperOrg
version = property("libBaseVersion") as String
base.archivesName.set(libMavenPublish)

/*
Use pre-installed Node.js
https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
 */
project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().download = true
}

/*
Reporting that yarn.lock has been updated
https://kotlinlang.org/docs/js-project-setup.html#reporting-that-yarn-lock-has-been-updated
 */
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(jvmToolchainVersion.toInt())

    androidTarget {
        publishLibraryVariants("release")
    }
    jvm()

    js(IR) {
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
        binaries.executable()
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    val xcframeworkName = libMavenPublish
    val xcf = XCFramework(xcframeworkName)
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        if (konanTarget.family.isAppleFamily) {
            binaries.framework {
                baseName = xcframeworkName
                binaryOption("bundleId", "$libDeveloperOrg.$xcframeworkName")
                xcf.add(this)
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }

        val commonMain by getting {
            kotlin.srcDirs(
                "core/src/commonMain/kotlin",
                "lines/src/commonMain/kotlin",
                "sites/src/commonMain/kotlin",
                "departures/src/commonMain/kotlin",
                "stoppoints/src/commonMain/kotlin",
                "authorities/src/commonMain/kotlin",
            )
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.koin.core)
                api(libs.koin.annotations)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kermit)
                implementation(libs.ktor.client.logging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            kotlin.srcDirs("core/src/androidMain/kotlin")
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.koin.android)
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val appleMain by creating {
            dependsOn(nativeMain)
        }
        val iosMain by creating {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
            dependsOn(appleMain)
            kotlin.srcDirs("core/src/iosMain/kotlin")
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val jvmMain by getting {
            kotlin.srcDirs("core/src/jvmMain/kotlin")
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val jsMain by getting {
            kotlin.srcDirs("core/src/jsMain/kotlin")
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
                freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
                freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsExport")
                freeCompilerArgs.add("-Xes-long-as-bigint")
            }
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = libDeveloperOrg
    compileSdk = (project.property("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (project.property("android.minSdk") as String).toInt()
    }
}

buildConfig {
    packageName("com.umain.transport.config")
    buildConfigField("String", "API_BASE_URL", "${project.property("serverHostURL")}")
    buildConfigField("String", "API_KEY", "${project.property("apiKey")}")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set(libMavenPublish)
                description.set(libDescription)
                url.set(libSiteUrl)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                scm {
                    url.set(libSiteUrl)
                    connection.set("scm:git:$libSiteUrl.git")
                    developerConnection.set("scm:git:$libGitUrl")
                }
            }
        }
    }
}

tasks.named("jsDevelopmentExecutableCompileSync") {
    dependsOn("jsNodeProductionRun")
    mustRunAfter("jsNodeProductionRun")
}

tasks.withType<KotlinWebpack>().configureEach {
    // We configure only the tasks related to the nodejs target
    if (name.startsWith("js") && name.endsWith("Webpack") && !name.contains("Browser")) {
        mainOutputFileName = "$libMavenPublish.js"
        output.libraryTarget = "commonjs2"
        outputDirectory =
            layout.buildDirectory
                .dir("js/packages/$libMavenPublish")
                .get()
                .asFile

        // Explicitly declare the dependency to fix validation errors
        val syncTaskName =
            if (name.contains("Production")) {
                "jsProductionLibraryCompileSync"
            } else {
                "jsDevelopmentLibraryCompileSync"
            }
        dependsOn(tasks.named(syncTaskName))
    }
}

tasks.register("printJsPackageDirs") {
    doLast {
        println("--- JS Package Information ---")
        val nodePackageDir =
            layout.buildDirectory
                .dir("js/packages/$libMavenPublish")
                .get()
                .asFile
        println("Node.js package directory: $nodePackageDir")
        println("Does it exist? ${nodePackageDir.exists()}")
        if (nodePackageDir.exists()) {
            println("Contents:")
            nodePackageDir.listFiles()?.forEach { println("  - ${it.name}") }
        }

        val browserDistDir =
            layout.buildDirectory
                .dir("dist/js/productionExecutable")
                .get()
                .asFile
        println("\nBrowser distribution directory: $browserDistDir")
        println("Does it exist? ${browserDistDir.exists()}")
        if (browserDistDir.exists()) {
            println("Contents:")
            browserDistDir.listFiles()?.forEach { println("  - ${it.name}") }
        }
        println("--------------------------")
    }
}

rootProject.layout.buildDirectory.dir("../build")
subprojects {
    project.layout.buildDirectory.dir("$rootProject.layout.buildDirectory/$project.name")
}

tasks.named("build") {
    finalizedBy("printJsPackageDirs")
}

tasks.named<Delete>("clean") {
    delete(
        rootProject.layout.buildDirectory
            .get()
            .asFile,
        file("build"),
        file("shared/build"),
        file("output"),
    )
}

tasks.register<Delete>("cleanAll") {
    delete(
        rootProject.layout.buildDirectory
            .get()
            .asFile,
        file("build"),
        file("shared/build"),
        file("output"),
    )
}

object DynamicVersion {
    fun setDynamicVersion(
        file: File,
        version: String,
    ) {
        val cleanedVersion = version.split('+')[0]
        file.writeText(cleanedVersion)
    }
}

tasks.register("versionFile") {
    val file = File(projectDir, "version.txt")

    DynamicVersion.setDynamicVersion(file, project.version.toString())
}

