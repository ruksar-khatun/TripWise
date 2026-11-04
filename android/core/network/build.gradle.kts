import java.util.Properties

plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

android {
    namespace = "com.tripwise.core.network"
    buildFeatures.buildConfig = true
    defaultConfig {
        val baseUrl = localProps.getProperty("BACKEND_BASE_URL") ?: "http://10.0.2.2:3000/"
        buildConfigField("String", "BACKEND_BASE_URL", "\"$baseUrl\"")
    }
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
}
