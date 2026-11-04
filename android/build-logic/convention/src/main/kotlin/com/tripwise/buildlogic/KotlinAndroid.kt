package com.tripwise.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * Shared Android configuration for application and library modules.
 * AGP 9 compiles Kotlin itself (built-in Kotlin), so no kotlin-android plugin is applied;
 * Kotlin's JVM target follows compileOptions.targetCompatibility.
 */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.intVersion("compileSdk")
        defaultConfig.minSdk = libs.intVersion("minSdk")
        compileOptions.sourceCompatibility = JAVA_VERSION
        compileOptions.targetCompatibility = JAVA_VERSION
    }
}
