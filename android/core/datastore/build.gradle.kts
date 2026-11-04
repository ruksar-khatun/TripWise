plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tripwise.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.network) // TokenStore contract
    implementation(libs.tink.android)
    implementation(libs.kotlinx.serialization.json)
}
