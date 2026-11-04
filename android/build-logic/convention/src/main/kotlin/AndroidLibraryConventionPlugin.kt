import com.android.build.api.dsl.LibraryExtension
import com.tripwise.buildlogic.configureKotlinAndroid
import com.tripwise.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
        }
        dependencies {
            add("testImplementation", libs.findLibrary("junit4").get())
        }
        // Full stack traces for failing tests in CI logs.
        tasks.withType(org.gradle.api.tasks.testing.Test::class.java).configureEach {
            testLogging {
                events("failed")
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }
}
