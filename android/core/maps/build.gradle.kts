plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.compose)
}

android {
    namespace = "com.tripwise.core.maps"
    testOptions.unitTests.isIncludeAndroidResources = true
}

// No Maps SDK and no API key: map features open Google Maps through public Maps URLs.
dependencies {
    implementation(projects.core.designsystem)
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}

// Robolectric reads java.io.FileDescriptor internals, which JDK 17+ blocks unless opened explicitly.
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    )
}
