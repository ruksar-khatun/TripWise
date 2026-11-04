plugins {
    alias(libs.plugins.tripwise.jvm.library)
    alias(libs.plugins.tripwise.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Pure Kotlin: no Android dependency, so it is unit-testable on the JVM with MockWebServer.
// The app supplies base URL / logging through NetworkConfig.
dependencies {
    api(projects.core.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    api(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
