@file:OptIn(ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
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
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.github.gmazzo.buildconfig")
    id("maven-publish")
    id("io.insert-koin.compiler.plugin")
    alias(libs.plugins.npm.publish)
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

    android {
        namespace = libDeveloperOrg
        compileSdk = (project.property("android.compileSdk") as String).toInt()
        minSdk = (project.property("android.minSdk") as String).toInt()
        withHostTestBuilder {}
    }
    jvm()

    js(IR) {
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
        // `binaries.executable()` is what the Node + SPA demos consume directly
        // via `file:` deps on the webpack output. Per JetBrains (Artem Kobzar,
        // mDevCamp 2026), for a *pure* publishable library the canonical value
        // is `binaries.library()`. Switch post-talk when the demo consumption
        // path is refactored — the `npm-publish` plugin emits a warning about
        // this when run against an executable target.
        binaries.executable()

        // Polish the auto-generated public package.json via the official
        // Kotlin/JS DSL — Artem Kobzar (JetBrains) flagged this as the
        // canonical approach. The `jsPublicPackageJson` task (default in the
        // multiplatform plugin) reads these customField entries and merges
        // them into the file the publish flow consumes. This replaces an
        // earlier custom `enhanceNpmPackageMetadata` task that rewrote the
        // file by hand; the DSL keeps us inside the Gradle dependency graph
        // without implicit_dependency validation errors.
        //
        // Reference: kotlinlang.org/docs/js-project-setup.html#package-json-customization
        // Reference: kotlinlang.org/docs/multiplatform/multiplatform-publish-libraries-to-npm.html
        //
        // INTENTIONALLY no `customField("type", "module")` — the Kotlin/JS
        // pipeline emits a CommonJS `webpack.config.js` alongside the package.
        // Declaring "type":"module" would force Node to import-parse that
        // config as ESM on the next jsBrowserProductionWebpack run and fail.
        // Consumers don't need it: `.mjs` extension is always ESM in Node,
        // and the `exports` map below routes imports explicitly.
        compilations["main"].packageJson {
            val entry = "kotlin/StockholmTransport-stockholm-transport.mjs"
            val types = "kotlin/StockholmTransport-stockholm-transport.d.mts"
            customField("name", "@jacksonmafra-umain/stockholm-transport")
            customField(
                "description",
                "Kotlin Multiplatform SDK for SL (Stockholms Lokaltrafik) — Android · iOS · JVM · Node · Browser.",
            )
            customField("main", entry)
            customField("module", entry)
            customField("types", types)
            customField(
                "exports",
                mapOf(
                    "." to mapOf(
                        "types" to "./$types",
                        "import" to "./$entry",
                        "default" to "./$entry",
                    ),
                    "./package.json" to "./package.json",
                ),
            )
            customField("files", listOf("kotlin/", "README.md"))
            customField(
                "keywords",
                listOf("kotlin", "multiplatform", "kmp", "sl", "stockholm", "transport", "sdk"),
            )
            customField("license", "Apache-2.0")
            customField(
                "repository",
                mapOf("type" to "git", "url" to libGitUrl),
            )
            customField(
                "publishConfig",
                mapOf(
                    "registry" to "https://npm.pkg.github.com",
                    "access" to "public",
                ),
            )
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Swift convention is UpperCamelCase for module names, so the iOS
    // XCFramework ships as "StockholmTransport" — matches the product
    // declared in the repo-root Package.swift. The Maven artifactId
    // (libMavenPublish = "stockholm-transport") stays in kebab case for
    // Maven/Gradle convention.
    val xcframeworkName = "StockholmTransport"
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
                "realtime/src/commonMain/kotlin",
            )
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.koin.core)
                api(libs.koin.annotations)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                // Realtime trip stream — TripUpdateDataSource opens a Ktor
                // WebSocket against the simulator's /updates/{tripId} path.
                implementation(libs.ktor.client.websockets)
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
                // -Xes-long-as-bigint was Kotlin 2.2's flag for mapping
                // Kotlin Long -> JS BigInt; Kotlin 2.3 dropped it (the
                // mapping is the default), so keeping it would emit
                // "Flag is not supported by this version of the compiler"
                // on every native link.
            }
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
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

// Tarball the polished package so `./sl publish` produces a real installable
// `.tgz` — what consumers would `npm install` from a registry. The polish
// itself lives in the `compilations["main"].packageJson { customField(...) }`
// DSL inside `kotlin { js(IR) { ... } }` above; it merges into the file the
// `jsPublicPackageJson` task emits, so by the time we npm-pack it, the file
// is already correct.
//
// Named `packTalkTgz` to avoid colliding with the `packJsPackage` task the
// org.danilopianini.npm.publish plugin registers (which would publish to a
// configured registry; we only want a local tarball for the demos).
tasks.register<Exec>("packTalkTgz") {
    description = "Creates an installable .tgz from the public JS package (the npmPack equivalent)."
    group = "build"

    // `jsPublicPackageJson` is the canonical Gradle task that emits the
    // public package.json with our customField entries applied. Depending on
    // it guarantees the file is current before we tarball.
    dependsOn("jsPublicPackageJson")
    dependsOn("jsProductionExecutableCompileSync")

    val packageDir = rootProject.layout.buildDirectory
        .dir("js/packages/StockholmTransport-stockholm-transport")
        .map { it.asFile }
    val outputDir = rootProject.layout.buildDirectory.dir("distributions/npm")

    workingDir = packageDir.get()
    commandLine("npm", "pack", "--pack-destination", outputDir.get().asFile.absolutePath)

    doFirst {
        outputDir.get().asFile.mkdirs()
        logger.lifecycle("Packing $packageDir → ${outputDir.get().asFile}")
    }
    doLast {
        logger.lifecycle("✓ npm tarball ready in ${outputDir.get().asFile}")
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

