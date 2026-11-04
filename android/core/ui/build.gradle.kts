plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.compose)
}

android {
    namespace = "com.tripwise.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    api(projects.core.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
