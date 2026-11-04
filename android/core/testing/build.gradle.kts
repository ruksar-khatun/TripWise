plugins {
    alias(libs.plugins.tripwise.jvm.library)
}

// Shared test infrastructure: fakes for every repository + coroutine rules.
// Consumed via testImplementation(projects.core.testing).
dependencies {
    api(projects.core.domain)
    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
