plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.hilt)
}

android {
    namespace = "com.tripwise.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(projects.core.common)
}
