plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.compose)
}

android {
    namespace = "com.tripwise.core.maps"
}

// The only module that touches the Google Maps SDK; features use the wrappers below.
dependencies {
    api(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(libs.maps.compose)
}
