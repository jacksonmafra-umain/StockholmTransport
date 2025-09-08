import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("umain.transport.kmp.core")
}

kotlin {
    val xcframeworkName = property("libMavenPublish") as String
    val xcf = XCFramework(xcframeworkName)

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        if (konanTarget.family.isAppleFamily) {
            binaries.framework {
                baseName = xcframeworkName
                export(project(":shared:core"))
                export(project(":shared:lines"))
                export(project(":shared:sites"))
                export(project(":shared:departures"))
                export(project(":shared:stoppoints"))
                export(project(":shared:authorities"))
                binaryOption("bundleId", "${property("libDeveloperOrg")}.$xcframeworkName")
                xcf.add(this)
            }
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