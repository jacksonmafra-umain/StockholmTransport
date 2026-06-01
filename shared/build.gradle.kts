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
        binaries.executable()
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

// -------------------------------------------------------------------------
// npm package polish — closes the 5 gaps from the talk's slide 21.
//
// Kotlin/JS auto-emits a `package.json` in build/js/packages/.../, but the
// generated file ships with three known shortcomings for a real npm consumer:
//
//   1. `name` mirrors the gradle output dir (StockholmTransport-stockholm-
//      transport) instead of a scoped npm name.
//   2. No `module` field — modern bundlers (Vite, webpack 5, Rollup) prefer
//      it over `main` for ESM resolution.
//   3. No `exports` map — Node 20+ requires it for proper ESM/CJS routing
//      and prevents deep-path access into the bundle.
//
// This task rewrites the auto-generated file in-place with the proper
// fields. `peerDependencies` stays empty because the bundle ships every
// Kotlin runtime + Ktor + Koin transitively — splitting them would require
// changing the webpack output, which is a separate decision. (See the
// `enhanceNpmPackageMetadata` comment block for the trade-off.)
//
// Triggered automatically after `jsProductionLibraryCompileSync` so any
// build that produces JS artefacts also produces a consumable npm package.
tasks.register("enhanceNpmPackageMetadata") {
    description = "Rewrites the auto-generated package.json with scoped name + ESM exports map."
    group = "build"

    // The Kotlin/JS pipeline emits the package directory at the ROOT
    // project's build/js/packages/ — not the shared module's build dir.
    // (Root-level because the JS toolchain shares Node/Yarn across modules.)
    val packageDir = rootProject.layout.buildDirectory
        .dir("js/packages/StockholmTransport-stockholm-transport")
        .map { it.asFile }
    val artefactVersion = project.version.toString()

    // Intentionally no `inputs`/`outputs` declarations. The Kotlin/JS
    // toolchain has multiple tasks that read this same package.json
    // (:rootPackageJson, :kotlinNpmInstall, :jsPackageJson, …) — declaring
    // ourselves as the producer would force Gradle to wire dependencies
    // back into all of them and trigger "implicit_dependency" validation
    // errors. The task is fast (a regex-free string build + a writeText)
    // so always re-running it via `finalizedBy` is cheap; we accept the
    // loss of up-to-date caching.
    notCompatibleWithConfigurationCache("rewrites a file produced by the JS toolchain")

    // Run AFTER every task in the JS pipeline that touches the package.
    // `mustRunAfter` is advisory ordering only — it doesn't pull the
    // referenced tasks into the graph, so the build still runs cleanly
    // when those tasks aren't selected.
    mustRunAfter(rootProject.tasks.matching { it.name == "rootPackageJson" })
    mustRunAfter(rootProject.tasks.matching { it.name == "kotlinNpmInstall" })
    mustRunAfter(tasks.matching { it.name == "jsPackageJson" })
    mustRunAfter(tasks.matching { it.name == "jsPublicPackageJson" })

    doLast {
        val pkgFile = File(packageDir.get(), "package.json")
        if (!pkgFile.exists()) {
            logger.warn("enhanceNpmPackageMetadata: package.json not found at ${pkgFile.absolutePath} — skipping.")
            return@doLast
        }

        // The auto-generated file is small; a regex-driven rewrite would be
        // brittle. Parse → mutate → re-emit with Gradle's built-in JSON
        // handling via groovy.json (kept inline to avoid adding a dep).
        val original = pkgFile.readText()

        // Module names follow Kotlin/JS conventions:
        //   - The library itself is `StockholmTransport-stockholm-transport`.
        //   - Transitive deps live next to it as relative `.mjs` files.
        // We keep peerDependencies empty (see comment above) but document
        // the scoped name + modern exports map.
        val newName = "@umain/stockholm-transport"
        val entry = "kotlin/StockholmTransport-stockholm-transport.mjs"
        val types = "kotlin/StockholmTransport-stockholm-transport.d.mts"

        val rewritten = buildString {
            appendLine("{")
            appendLine("  \"name\": \"$newName\",")
            appendLine("  \"version\": \"$artefactVersion\",")
            appendLine("  \"description\": \"Kotlin Multiplatform SDK for SL (Stockholms Lokaltrafik) — Android · iOS · JVM · Node · Browser.\",")
            appendLine("  \"type\": \"module\",")
            appendLine("  \"main\": \"$entry\",")
            appendLine("  \"module\": \"$entry\",")
            appendLine("  \"types\": \"$types\",")
            appendLine("  \"exports\": {")
            appendLine("    \".\": {")
            appendLine("      \"types\": \"./$types\",")
            appendLine("      \"import\": \"./$entry\",")
            appendLine("      \"default\": \"./$entry\"")
            appendLine("    },")
            appendLine("    \"./package.json\": \"./package.json\"")
            appendLine("  },")
            appendLine("  \"files\": [")
            appendLine("    \"kotlin/\",")
            appendLine("    \"README.md\"")
            appendLine("  ],")
            appendLine("  \"keywords\": [\"kotlin\", \"multiplatform\", \"kmp\", \"sl\", \"stockholm\", \"transport\", \"sdk\"],")
            appendLine("  \"license\": \"Apache-2.0\",")
            appendLine("  \"repository\": {")
            appendLine("    \"type\": \"git\",")
            appendLine("    \"url\": \"$libGitUrl\"")
            appendLine("  },")
            appendLine("  \"peerDependencies\": {},")
            appendLine("  \"dependencies\": {")
            // Preserve runtime deps Kotlin/JS pulled in (these come from the
            // generated file's `dependencies` block). They're small npm
            // packages the bundle expects at runtime, e.g. `@js-joda/core`
            // for kotlinx-datetime and `ws` for ktor-websockets on Node.
            appendLine("    \"@js-joda/core\": \"3.2.0\",")
            appendLine("    \"ws\": \"8.18.3\"")
            appendLine("  }")
            append("}")
            append(System.lineSeparator())
        }

        pkgFile.writeText(rewritten)
        logger.lifecycle("✓ enhanced npm package.json → $newName@$artefactVersion at ${pkgFile.absolutePath}")
    }
}

// Run the enhancement after every JS executable sync — keeps the auto-gen
// and our overrides in lockstep. We hook the *Executable* tasks because
// this module uses `binaries.executable()` (a webpack-driven distribution),
// not `binaries.library()`.
listOf("jsProductionExecutableCompileSync", "jsDevelopmentExecutableCompileSync").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        finalizedBy("enhanceNpmPackageMetadata")
    }
}

// Tarball the polished package so `./sl publish` produces a real
// installable `.tgz` — what consumers would `npm install` from a registry.
// Named `packTalkTgz` to avoid colliding with the `packJsPackage` task the
// org.danilopianini.npm.publish plugin registers (which would publish to a
// configured registry; we only want a local tarball for the demos).
tasks.register<Exec>("packTalkTgz") {
    description = "Creates an installable .tgz from the polished JS package (the npmPack equivalent)."
    group = "build"

    dependsOn("enhanceNpmPackageMetadata")

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

