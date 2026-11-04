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

        // AI assistant (Gemini via backend; shows "not set up" if the server has no key)
        t.scrollTo("Ask TripWise")
        t.click("Ask TripWise")
        t.waitFor("How can I help you travel?")
        t.shot("assistant-empty", settleMs = 1_000)
        t.click("Where should I go in December?", required = false)
        runCatching { t.waitFor("Thinking…", timeoutMs = 5_000) }
        Thread.sleep(12_000) // let the streamed reply arrive
        t.shot("assistant-reply", settleMs = 1_000)
        t.back()

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

        // Things to do (curated + nearby from Google Places) and the map
        t.scrollTo("Things to do")
        t.shot("destination-things-to-do", settleMs = 2_000)
        if (t.click("See all", required = false)) {
            t.waitFor("Things to do in", substring = true)
            t.shot("activities-curated", settleMs = 1_500)
            t.click("Nearby now", required = false)
            t.shot("activities-nearby", settleMs = 5_000)
            t.back()
        }
        // AI itinerary planner → save as a new trip
        t.scrollTo("Plan with AI")
        if (t.click("Plan with AI", required = false)) {
            t.waitFor("Generate itinerary")
            t.shot("planner-preferences", settleMs = 1_000)
            t.click("Generate itinerary")
            runCatching { t.waitFor("Day 1", timeoutMs = 60_000) }
            t.shot("planner-result", settleMs = 1_500)
            t.swipeUp()
            t.shot("planner-result-scrolled", settleMs = 1_000)
            if (t.click("Save as new trip", required = false)) {
                runCatching { t.waitFor("Overview", timeoutMs = 10_000) }
                t.shot("trip-from-plan-overview", settleMs = 2_500)
                t.click("Itinerary", required = false)
                t.shot("trip-from-plan-itinerary", settleMs = 1_500)
                t.click("Budget", required = false)
                t.click("Estimate", required = false)
                runCatching { t.waitFor("Estimated total", timeoutMs = 15_000) }
                t.shot("trip-budget-estimate", settleMs = 1_500)
                t.click("Use as my budget", required = false)
                t.swipeUp()
                t.shot("trip-budget-plan", settleMs = 1_500)
                t.back()
            } else {
                t.back()
            }
        }
        t.scrollTo("On the map")
        t.shot("destination-map-preview", settleMs = 4_000)
        if (t.clickDescription("Open map")) {
            t.shot("destination-map", settleMs = 7_000)
            t.back()
        }
        t.back()

        // Saved
        t.click("Saved")
        t.shot("saved", settleMs = 3_000)

        // My Trips → create
        t.click("My Trips")
        t.shot("trips-empty", settleMs = 2_000)
        if (!t.click("New trip", required = false)) t.click("Plan a trip")
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
