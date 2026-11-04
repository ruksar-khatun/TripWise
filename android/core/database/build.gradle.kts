plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.room)
    alias(libs.plugins.tripwise.hilt)
}

android {
    namespace = "com.tripwise.core.database"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
