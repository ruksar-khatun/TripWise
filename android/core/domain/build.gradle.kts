plugins {
    alias(libs.plugins.tripwise.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    testImplementation(libs.kotlinx.coroutines.test)
}
