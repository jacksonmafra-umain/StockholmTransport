import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
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
}

group = libDeveloperOrg
version = libBaseVersion
base.archivesName.set(libMavenPublish)

/*
Use pre-installed Node.js
https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
 */
project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().download = false
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
kotlin {
    jvmToolchain(jvmToolchainVersion.toInt())

    androidTarget {
        publishLibraryVariants("release")
    }
    jvm()
    js(IR) {
        outputModuleName = libMavenPublish

        compilations["main"].packageJson {
            customField("generated", mapOf("one" to 1, "two" to 2))
            val now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            customField("buildTimestamp", now)
        }

        /*
        Distribution target directory
        By default, the results of a Kotlin/JS project build reside in the
        /build/dist/<targetName>/<binaryName> directory within the project root.
         */
        useEsModules()
        nodejs(){
            distribution {
                outputDirectory.set(projectDir.resolve("output"))
            }
        }
        browser {
            distribution {
                outputDirectory.set(projectDir.resolve("output"))
            }
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
                outputFileName = "$libMavenPublish.js"
                sourceMaps = false
            }
        }
        generateTypeScriptDefinitions()
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
        compilations["main"].packageJson {
            name = "$libMavenPublish"
            version = "$libBaseVersion"
            main = "$libMavenPublish.js"
            customField("repository", mapOf("type" to "git", "url" to libSiteUrl))
            customField("author", "$developerName <$developerEmail>")
        }
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
        val commonMain by getting {
            kotlin.srcDirs(
                "core/src/commonMain/kotlin",
                "lines/src/commonMain/kotlin",
                "sites/src/commonMain/kotlin",
                "departures/src/commonMain/kotlin",
                "stoppoints/src/commonMain/kotlin",
                "authorities/src/commonMain/kotlin"
            )
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.koin.core)
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