import java.util.Properties

plugins {
    alias(libs.plugins.tripwise.android.application)
    alias(libs.plugins.tripwise.android.compose)
    alias(libs.plugins.tripwise.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

android {
    namespace = "com.tripwise.app"

    defaultConfig {
        applicationId = "com.tripwise.app"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.buildConfig = true

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            val debugUrl = localProps.getProperty("BACKEND_BASE_URL") ?: "http://10.0.2.2:3000/"
            buildConfigField("String", "BACKEND_BASE_URL", "\"$debugUrl\"")
        }
        release {
            // Must be HTTPS (release network security config blocks cleartext).
            val releaseUrl = localProps.getProperty("BACKEND_BASE_URL_RELEASE") ?: "https://api.tripwise.invalid/"
            buildConfigField("String", "BACKEND_BASE_URL", "\"$releaseUrl\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.network)
    implementation(projects.core.domain)

    implementation(projects.feature.auth)
    implementation(projects.feature.home)
    implementation(projects.feature.explore)
    implementation(projects.feature.saved)
    implementation(projects.feature.destination)
    implementation(projects.feature.trips)
    implementation(projects.feature.itinerary)
    implementation(projects.feature.assistant)
    implementation(projects.feature.profile)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)

    // Emulator tour (scripted walkthrough with screenshots), run by .github/workflows/demo.yml
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
