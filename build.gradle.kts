plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.buildconfig).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.koin.compiler).apply(false)
}
