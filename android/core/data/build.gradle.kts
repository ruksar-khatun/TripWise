plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.hilt)
}

android {
    namespace = "com.tripwise.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.network)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    // Room is added (with the androidx.room + KSP setup) when the first entity is introduced.
    testImplementation(libs.kotlinx.coroutines.test)
}
