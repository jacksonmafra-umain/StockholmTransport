plugins {
    id("umain.transport.kmp.core")
    id("maven-publish")
}

group = property("libDeveloperOrg") as String
version = property("libBaseVersion") as String

val exportedProjects =
    listOf(
        ":shared:core",
        ":shared:lines",
        ":shared:sites",
        ":shared:departures",
        ":shared:stoppoints",
        ":shared:authorities",
    )

kotlin {
    androidTarget {
        mavenPublication {
            artifactId = property("libMavenPublish") as String
        }
    }
    // set to suppress: 'expect'/'actual' classes (including interfaces, objects, annotations, enums, and 'actual'
    // typealiases) are in Beta. You can use -Xexpect-actual-classes flag to suppress this warning.
    // Also see: https://youtrack.jetbrains.com/issue/KT-61573
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            exportedProjects.forEach { api(project(":$it")) }
        }
    }
}

android {

    defaultConfig {
        consumerProguardFile(File("multiplatform-proguard.pro"))
    }

    publishing {
        singleVariant("release") {
            // if you don't want sources/javadoc, remove these lines
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        getByName<MavenPublication>("kotlinMultiplatform") {
            artifactId = property("libMavenPublish") as String

            pom {
                name.set(property("libMavenPublish") as String)
                description.set(property("libDescription") as String)
                url.set(property("libSiteUrl") as String)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set(property("developerId") as String)
                        name.set(property("developerName") as String)
                        email.set(property("developerEmail") as String)
                    }
                }
                scm {
                    url.set(property("libSiteUrl") as String)
                    connection.set("scm:git:${property("libSiteUrl")}.git")
                    developerConnection.set("scm:git:${property("libGitUrl")}")
                }
            }
        }
    }
}
