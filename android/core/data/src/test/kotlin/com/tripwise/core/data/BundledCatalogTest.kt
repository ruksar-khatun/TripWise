package com.tripwise.core.data

import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** The APK's offline catalog must stay identical to the backend's seed file. */
class BundledCatalogTest {
    @Test fun `bundled catalog matches backend catalog`() {
        val bundled = File("src/main/assets/catalog/destinations.json")
        val backend = File("../../../backend/catalog/destinations.json")
        assumeTrue("backend folder not present", backend.exists())
        assertEquals(
            "Copy backend/catalog/destinations.json to android/core/data/src/main/assets/catalog/",
            backend.readText(), bundled.readText(),
        )
    }
}
