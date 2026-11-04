plugins {
    alias(libs.plugins.tripwise.jvm.library)
    alias(libs.plugins.tripwise.hilt)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)
    testImplementation(libs.kotlinx.coroutines.test)
}
