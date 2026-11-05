package com.tripwise.core.maps

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class GoogleMapsLinksTest {
    @Test fun `builds key-free search and directions urls`() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=Amber%20Fort%2C%20Jaipur%2C%20Rajasthan",
            GoogleMapsLinks.searchUrl("Amber Fort, Jaipur, Rajasthan"),
        )
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=Taj%20Mahal%2C%20Agra",
            GoogleMapsLinks.directionsUrl("Taj Mahal, Agra"),
        )
        assertEquals("Restaurants near Jaipur, Rajasthan, India", GoogleMapsLinks.nearbyQuery("Restaurants", placeLabel("Jaipur", "Rajasthan", "India")))
    }

    @Test fun `place label skips blanks and duplicates`() {
        assertEquals("Goa, India", placeLabel("Goa", "Goa", "India"))
        assertEquals("Delhi", placeLabel("Delhi", "", null))
    }

    @Test fun `opens google maps when nothing else is installed falls back to any viewer`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        // Robolectric resolves activities leniently; the first attempt targets the Google Maps package.
        assertTrue(openSearch(app, "Hawa Mahal, Jaipur"))
        val started = shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertTrue(started.data.toString().startsWith("https://www.google.com/maps/search/?api=1&query=Hawa"))
    }
}
