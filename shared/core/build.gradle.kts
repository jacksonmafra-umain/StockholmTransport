plugins {
    id("umain.transport.kmp.core")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
    }
}

buildConfig {
    packageName("com.umain.transport.config")
    buildConfigField("String", "API_BASE_URL", "${project.property("serverHostURL")}")
}
