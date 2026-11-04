package com.tripwise.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tripwise.core.database.entity.ExpenseEntity
import com.tripwise.core.database.entity.ItineraryItemEntity
import com.tripwise.core.database.entity.SavedDestinationEntity
import com.tripwise.core.database.entity.SyncState
import com.tripwise.core.database.entity.TripEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class DaoTest {
    private lateinit var db: TripWiseDatabase
    private val now = Instant.parse("2026-03-01T10:00:00Z")

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), TripWiseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After fun tearDown() = db.close()

    private fun trip(id: String, start: String) = TripEntity(
        id, "Trip $id", null, "Kyoto", null, LocalDate.parse(start), LocalDate.parse(start).plusDays(2), 2,
        100_000, "JPY", null, now, now,
    )

    @Test fun `trips are ordered by start date and soft delete hides them`() = runTest {
        val dao = db.tripDao()
        dao.insert(trip("b", "2026-05-01"))
        dao.insert(trip("a", "2026-04-01"))
        assertEquals(listOf("a", "b"), dao.observeAll().first().map { it.id })

        dao.markDeleted("a", now)
        assertEquals(listOf("b"), dao.observeAll().first().map { it.id })
        assertNull(dao.observeById("a").first())
    }

    @Test fun `dates and times round-trip and done flag marks row pending`() = runTest {
        db.tripDao().insert(trip("t", "2026-04-01"))
        val item = ItineraryItemEntity("i", "t", LocalDate.parse("2026-04-02"), LocalTime.of(9, 30), "Temple",
            "SIGHTSEEING", 500, "JPY", false, null, now, SyncState.SYNCED)
        db.itineraryDao().upsert(listOf(item))
        db.itineraryDao().setDone("i", true, now)
        val read = db.itineraryDao().observeForTrip("t").first().single()
        assertEquals(LocalTime.of(9, 30), read.startTime)
        assertTrue(read.isDone)
        assertEquals(SyncState.PENDING, read.syncState)
    }

    @Test fun `spent per category sums non-deleted expenses`() = runTest {
        db.tripDao().insert(trip("t", "2026-04-01"))
        db.openHelper.writableDatabase.apply {
            listOf(
                ExpenseEntity("e1", "t", "FOOD", 1_000, "JPY", null, LocalDate.parse("2026-04-01"), now),
                ExpenseEntity("e2", "t", "FOOD", 2_500, "JPY", null, LocalDate.parse("2026-04-01"), now),
                ExpenseEntity("e3", "t", "FOOD", 9_999, "JPY", null, LocalDate.parse("2026-04-01"), now, deleted = true),
            ).forEach { e ->
                execSQL(
                    "INSERT INTO expenses (id, trip_id, category, amount_minor, currency, note, spent_on, updated_at, sync_state, deleted) " +
                        "VALUES (?, ?, ?, ?, ?, NULL, ?, ?, 'PENDING', ?)",
                    arrayOf<Any?>(e.id, e.tripId, e.category, e.amountMinor, e.currency, e.spentOn.toString(), now.toEpochMilli(), if (e.deleted) 1 else 0),
                )
            }
        }
        val totals = db.budgetDao().observeSpentByCategory("t").first()
        assertEquals(3_500L, totals.single { it.category == "FOOD" }.totalMinor)
    }

    @Test fun `deleting a trip row cascades to its items`() = runTest {
        db.tripDao().insert(trip("t", "2026-04-01"))
        db.itineraryDao().upsert(listOf(ItineraryItemEntity("i", "t", LocalDate.parse("2026-04-01"), null, "x", "OTHER", null, null, false, null, now)))
        db.openHelper.writableDatabase.execSQL("DELETE FROM trips WHERE id = 't'")
        assertEquals(emptyList<ItineraryItemEntity>(), db.itineraryDao().observeForTrip("t").first())
    }

    @Test fun `saved destinations upsert and unsave`() = runTest {
        val dao = db.savedDestinationDao()
        dao.upsert(SavedDestinationEntity("kyoto", now))
        dao.upsert(SavedDestinationEntity("bali", now.plusSeconds(60)))
        assertEquals(listOf("bali", "kyoto"), dao.observeAll().first().map { it.destinationId })
        dao.markDeleted("bali")
        dao.upsert(SavedDestinationEntity("kyoto", now)) // re-save after delete clears the flag
        assertEquals(listOf("kyoto"), dao.observeAll().first().map { it.destinationId })
    }
}
