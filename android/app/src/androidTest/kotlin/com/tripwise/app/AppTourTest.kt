package com.tripwise.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Scripted walkthrough of the real app against a real backend (started by the demo workflow),
 * capturing a screenshot at each step. Not a correctness suite: steps tolerate missing content
 * so one change doesn't hide every later screenshot.
 */
@RunWith(AndroidJUnit4::class)
class AppTourTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun tour() {
        val t = Tour(rule)

        // Authentication
        t.waitFor("Welcome back", timeoutMs = 30_000)
        t.shot("sign-in")
        t.click("Create an account")
        t.type("Your name", "Alex Traveller")
        t.type("Email", "alex+${System.currentTimeMillis()}@example.com")
        t.type("Password", "correct horse battery")
        t.shot("sign-up")
        t.click("Create account")

        // Home
        t.waitFor("Where to next?", timeoutMs = 30_000)
        t.shot("home", settleMs = 4_000)
        t.swipeUp()
        t.shot("home-scrolled", settleMs = 2_500)

        // Explore → destination detail
        t.click("Explore")
        t.waitFor("destinations", substring = true)
        t.shot("explore", settleMs = 4_000)
        t.click("Kyoto")
        t.waitFor("Why visit")
        t.shot("destination-detail", settleMs = 4_000)
        t.swipeUp()
        t.shot("destination-detail-scrolled", settleMs = 2_500)
        t.clickDescription("Save Kyoto")
        t.shot("destination-saved", settleMs = 1_000)
        t.back()

        // Saved
        t.click("Saved")
        t.shot("saved", settleMs = 3_000)

        // My Trips → create
        t.click("My Trips")
        t.shot("trips-empty", settleMs = 2_000)
        t.click("New trip")
        t.type("Trip name", "Kyoto in Spring")
        t.type("Destination", "Kyoto")
        t.shot("create-trip")
        t.back()
        t.back()

        // Profile
        t.click("Profile")
        t.waitFor("Sign out")
        t.shot("profile", settleMs = 2_000)
    }
}
