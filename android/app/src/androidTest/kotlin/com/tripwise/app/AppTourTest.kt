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

        // TripMate AI (Gemini via backend; shows "not set up" if the server has no key)
        t.scrollTo("Ask TripMate AI")
        t.click("Ask TripMate AI")
        t.waitFor("Hi, I'm TripMate AI")
        t.shot("tripmate-intro", settleMs = 1_000)
        if (t.click("Set budget, days and interests", required = false)) {
            runCatching { t.waitFor("Trip preferences") }
            t.click("Mid-range", required = false)
            t.click("Food", required = false)
            t.shot("tripmate-preferences", settleMs = 1_000)
            t.click("Apply", required = false)
        }
        t.click("Where in India should I go in December?", required = false)
        runCatching { t.waitFor("Thinking…", timeoutMs = 5_000) }
        Thread.sleep(12_000) // let the streamed reply arrive
        t.shot("tripmate-reply", settleMs = 1_000)
        if (t.clickDescription("Chat history")) {
            runCatching { t.waitFor("Where in India should I go in December?", timeoutMs = 5_000) }
            t.shot("tripmate-history", settleMs = 1_000)
            t.back()
        }
        t.back()

        // Explore → destination detail
        t.click("Explore")
        t.waitFor("destinations", substring = true)
        t.shot("explore", settleMs = 4_000)
        t.click("Jaipur")
        t.waitFor("Why visit")
        t.shot("destination-detail", settleMs = 4_000)
        t.swipeUp()
        t.shot("destination-detail-scrolled", settleMs = 2_500)
        t.clickDescription("Save Jaipur")
        t.shot("destination-saved", settleMs = 1_000)

        // Things to do (curated + Google Maps hand-off; no Maps API key needed)
        t.scrollTo("Things to do")
        t.shot("destination-things-to-do", settleMs = 2_000)
        if (t.click("See all", required = false)) {
            t.waitFor("Things to do in", substring = true)
            t.shot("activities-curated", settleMs = 1_500)
            t.click("On Google Maps", required = false)
            t.shot("activities-google-maps", settleMs = 1_500)
            t.back()
        }
        t.scrollTo("Explore Jaipur on Google Maps")
        t.shot("destination-google-maps-card", settleMs = 1_000)
        // TripMate AI from the destination: restaurant cards linked to Google Maps
        t.scrollTo("Where to eat")
        t.shot("destination-tripmate", settleMs = 1_000)
        if (t.click("Where to eat", required = false)) {
            runCatching { t.waitFor("Where should I eat in Jaipur?", timeoutMs = 10_000) }
            runCatching { t.waitFor("Open in Google Maps", timeoutMs = 45_000) }
            t.shot("tripmate-restaurants", settleMs = 1_500)
            t.back()
        }
        // AI itinerary planner → save as a new trip
        t.scrollTo("Plan with AI")
        if (t.click("Plan with AI", required = false)) {
            t.waitFor("Destination")
            t.shot("planner-preferences", settleMs = 1_000)
            t.scrollTo("Generate itinerary")
            t.click("Generate itinerary")
            runCatching { t.waitFor("Day 1", timeoutMs = 60_000) }
            t.shot("planner-result", settleMs = 1_500)
            t.swipeUp()
            t.shot("planner-result-scrolled", settleMs = 1_000)
            if (t.click("Save as new trip", required = false)) {
                runCatching { t.waitFor("Overview", timeoutMs = 10_000) }
                t.shot("trip-from-plan-overview", settleMs = 2_500)
                t.scrollTo("Ask TripMate AI about this trip")
                if (t.click("Ask TripMate AI about this trip", required = false)) {
                    runCatching { t.waitFor("Review my itinerary and suggest improvements") }
                    t.shot("tripmate-trip", settleMs = 1_500)
                    t.back()
                }
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
        t.back()

        // Saved
        t.click("Saved")
        t.shot("saved", settleMs = 3_000)

        // My Trips → create
        t.click("My Trips", required = false)
        t.shot("my-trips", settleMs = 2_000)
        if (t.click("New trip", required = false) || t.click("Plan a trip", required = false)) {
            t.type("Trip name", "Udaipur by the lakes", required = false)
            t.type("Destination", "Udaipur", required = false)
            t.shot("create-trip")
            t.back()
            t.back()
        }

        // Profile
        t.click("Profile", required = false)
        runCatching { t.waitFor("Sign out") }
        t.shot("profile", settleMs = 2_000)
    }
}
