plugins {
    alias(libs.plugins.tripwise.android.feature)
}

dependencies {
    implementation(projects.core.maps)
}

android {
    namespace = "com.tripwise.feature.destination"
}
