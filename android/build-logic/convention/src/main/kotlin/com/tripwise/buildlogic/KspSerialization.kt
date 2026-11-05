package com.tripwise.buildlogic

import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/** Shared lock: at most one KSP task runs at a time across the whole build. */
abstract class KspLock : BuildService<BuildServiceParameters.None>

/**
 * KSP2 processors running in parallel in one Gradle daemon intermittently see types from
 * dependency modules (e.g. core:domain repositories) as unresolved, so Hilt fails or, worse,
 * silently skips a ViewModel. Serialising KSP tasks costs little and removes the race; KSP
 * outputs are also kept out of the build cache so an incomplete result is never reused.
 */
internal fun Project.serializeKspTasks() {
    val lock = gradle.sharedServices.registerIfAbsent("tripwiseKspLock", KspLock::class.java) {
        maxParallelUsages.set(1)
    }
    tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("Kotlin") }.configureEach {
        usesService(lock)
        // A racy run could "succeed" with incomplete Hilt output; never reuse KSP results from the build cache.
        outputs.doNotCacheIf("KSP output must come from a fresh, serialised run") { true }
    }
}
