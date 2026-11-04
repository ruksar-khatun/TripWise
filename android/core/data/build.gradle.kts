plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tripwise.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
}
