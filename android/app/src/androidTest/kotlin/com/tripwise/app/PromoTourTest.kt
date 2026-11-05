package com.tripwise.app

import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Paced walkthrough of the real app, screen-recorded one clip per feature for the promo video
 * (.github/workflows/promo.yml). Only real screens and interactions; optional steps tolerate
 * missing content so one change doesn't lose every later clip.
 */
@RunWith(AndroidJUnit4::class)
class PromoTourTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val automation get() = InstrumentationRegistry.getInstrumentation().uiAutomation
    private var recording: ParcelFileDescriptor? = null
    private var clip = 0

    private fun shell(command: String) {
        automation.executeShellCommand(command).use { pfd ->
            ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes() } // wait for completion
        }
    }

    /** Starts `screenrecord` for one feature clip (saved under /sdcard/Download/promo). */
    private fun start(name: String) {
        stop()
        val file = "/sdcard/Download/promo/%02d-%s.mp4".format(++clip, name)
        recording = automation.executeShellCommand("screenrecord --bit-rate 10000000 --time-limit 120 $file")
        Thread.sleep(1_200) // encoder warm-up so the first frames aren't lost
        Log.i(TAG, "recording $file")
    }

    private fun stop() {
        val r = recording ?: return
        Thread.sleep(700)
        shell("pkill -INT screenrecord")
        Thread.sleep(1_500) // let screenrecord finalise the MP4
        runCatching { r.close() }
        recording = null
    }

    private fun pause(ms: Long = 1_200) {
        rule.waitForIdle()
        Thread.sleep(ms)
    }

    @Test fun promo() {
        val t = Tour(rule)
        shell("rm -rf /sdcard/Download/promo")
        shell("mkdir -p /sdcard/Download/promo")
        try {
            // 1. Authentication
            t.waitFor("Welcome back", timeoutMs = 30_000)
            start("auth")
            pause(1_500)
            t.click("Create an account"); pause(900)
            t.type("Your name", "Alex Traveller"); pause(500)
            t.type("Email", "alex+${System.currentTimeMillis()}@example.com"); pause(500)
            t.type("Password", "correct horse battery"); pause(900)
            t.click("Create account")
            t.waitFor("Where to next?", timeoutMs = 30_000)
            pause(2_500)

            // 2. Home
            start("home")
            pause(2_000)
            t.swipeUp(); pause(1_500)
            t.swipeUp(); pause(1_500)

            // 3. TripMate AI: welcome, quick actions, personalised preferences
            start("tripmate")
            t.scrollTo("Ask TripMate AI")
            t.click("Ask TripMate AI")
            t.waitFor("Hi, I'm TripMate AI")
            pause(2_200)
            if (t.click("Set budget, days and interests", required = false)) {
                pause(1_200)
                t.click("Mid", required = false); pause(600)
                t.clickDescription("Increase Days"); pause(300)
                t.clickDescription("Increase Days"); pause(300)
                t.clickDescription("Increase Days"); pause(500)
                t.clickDescription("Increase Travellers"); pause(300)
                t.clickDescription("Increase Travellers"); pause(500)
                t.click("Food", required = false); pause(400)
                t.click("Culture", required = false); pause(1_000)
                t.click("Apply", required = false)
            }
            pause(2_000)
            t.back(); pause(800)

            // 4. Explore destinations
            start("explore")
            t.click("Explore")
            t.waitFor("destinations", substring = true)
            pause(2_000)
            t.click("Beaches", required = false); pause(1_600)
            t.click("Culture", required = false); pause(1_600)
            t.click("All", required = false); pause(1_000)
            t.click("Jaipur")
            t.waitFor("Why visit")
            pause(2_500)

            // 5. Destination details, recommendations, save
            start("destination")
            pause(1_000)
            t.clickDescription("Save Jaipur"); pause(1_000)
            repeat(4) { t.swipeUp(); pause(1_300) }

            // 6. Things to do + stays / restaurants / directions via Google Maps
            start("activities")
            t.scrollTo("See all")
            t.click("See all", required = false)
            runCatching { t.waitFor("Things to do in", substring = true) }
            pause(1_800)
            t.click("Sightseeing", required = false); pause(1_500)
            t.click("All", required = false); pause(800)
            t.click("On Google Maps", required = false); pause(3_000)
            t.back(); pause(800)
            t.scrollTo("Explore Jaipur on Google Maps")
            pause(2_500)
            t.scrollTo("Where to eat")
            pause(2_000)

            // 7. Itinerary planner → save as trip
            start("planner")
            if (t.click("Plan with AI", required = false)) {
                t.waitFor("Destination")
                pause(1_500)
                t.click("Packed", required = false); pause(700)
                t.click("Food", required = false); pause(700)
                t.scrollTo("Generate itinerary"); pause(600)
                t.click("Generate itinerary")
                runCatching { t.waitFor("Day 1", timeoutMs = 60_000) }
                pause(2_500)
                repeat(3) { t.swipeUp(); pause(1_200) }
                pause(800)
                start("trip")
                t.click("Save as new trip", required = false)
                runCatching { t.waitFor("Overview", timeoutMs = 10_000) }
                pause(2_500)
                t.click("Itinerary", required = false); pause(1_800)
                t.click("Day 2", required = false); pause(1_500)
                t.click("Budget", required = false); pause(1_200)
                t.click("Estimate", required = false)
                runCatching { t.waitFor("Estimated total", timeoutMs = 15_000) }
                pause(2_000)
                t.click("Use as my budget", required = false); pause(1_500)
                t.swipeUp(); pause(2_000)
                t.back(); pause(800)
            }
            t.back(); pause(800)

            // 8. My Trips, Saved, Profile
            start("tabs")
            t.click("My Trips", required = false); pause(2_500)
            t.click("Saved", required = false); pause(2_200)
            t.click("Profile", required = false); pause(2_500)
            stop()
        } finally {
            stop()
        }
    }

    private companion object { const val TAG = "PromoTour" }
}
