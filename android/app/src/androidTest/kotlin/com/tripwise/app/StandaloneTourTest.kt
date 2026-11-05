package com.tripwise.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs with NO backend (apk.yml): proves the installable APK works on its own on a phone.
 * Guest mode → bundled catalog → destination → on-device itinerary → save trip → budget estimate.
 */
@RunWith(AndroidJUnit4::class)
class StandaloneTourTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun standalone() {
        val t = Tour(rule)
        t.waitFor("Welcome back", timeoutMs = 30_000)
        t.scrollTo("Continue as guest")
        t.shot("standalone-sign-in")
        t.click("Continue as guest")
        android.util.Log.i("Session", "test: activity is ${rule.activity.lifecycle.currentState}")
        t.shot("standalone-after-guest", settleMs = 3_000)

        t.waitFor("Where to next?", timeoutMs = 30_000)
        t.waitFor("Jaipur", timeoutMs = 30_000) // bundled catalog, no server
        t.shot("standalone-home", settleMs = 4_000)

        t.click("Explore")
        t.waitFor("16 destinations", substring = true)
        t.click("Jaipur")
        t.waitFor("Why visit")
        t.shot("standalone-destination", settleMs = 3_000)

        t.scrollTo("Plan with AI")
        t.click("Plan with AI")
        t.waitFor("Destination")
        t.scrollTo("Generate itinerary")
        t.click("Generate itinerary")
        t.waitFor("Day 1", timeoutMs = 60_000) // server unreachable → curated plan built on the phone
        t.shot("standalone-plan", settleMs = 1_500)
        t.swipeUp()
        t.click("Save as new trip")
        t.waitFor("Overview", timeoutMs = 15_000)
        t.click("Budget")
        t.click("Estimate")
        t.waitFor("Estimated total", timeoutMs = 15_000) // fallback exchange rates
        t.shot("standalone-budget", settleMs = 1_500)
        t.back()

        t.click("Profile")
        t.waitFor("Guest traveller", timeoutMs = 15_000)
        t.scrollTo("Create account or sign in")
        t.shot("standalone-profile", settleMs = 1_500)
    }
}
