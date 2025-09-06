import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        publishLibraryVariants("release")
    }

    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    js(IR) {
        useEsModules()
        nodejs()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }

    val xcframeworkName = "StockholmTransport"
    val xcf = XCFramework(xcframeworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcframeworkName

            export(project(":shared:core"))
            export(project(":shared:lines"))
            export(project(":shared:sites"))
            export(project(":shared:departures"))
            export(project(":shared:stoppoints"))
            export(project(":shared:authorities"))

            binaryOption("bundleId", "com.umain.transport.StockholmTransport")

            xcf.add(this)
        }
    }



    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core"))
            api(project(":shared:lines"))
            api(project(":shared:sites"))
            api(project(":shared:departures"))
            api(project(":shared:stoppoints"))
            api(project(":shared:authorities"))
        }
    }
}

android {
    namespace = "com.umain.transport"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

mavenPublishing {
    coordinates("com.umain.transport", "stockholm-transport", "1.0.0")

    pom {
        name.set("Stockholm Transport KMP Library")
        description.set("A KMP library for the Stockholm public transport API.")
        url.set("https://github.com/eidra-umain/stockholm-transport")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("jacksonmafra-umain")
                name.set("Jackson Mafra")
                email.set("jackson.mafra@umain.com")
            }
        }
        scm {
            url.set("https://github.com/eidra-umain/stockholm-transport")
            connection.set("scm:git:git://github.com/eidra-umain/stockholm-transport.git")
            developerConnection.set("scm:git:ssh://git@github.com:eidra-umain/stockholm-transport.git")
        }
    }
}