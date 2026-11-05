package com.tripwise.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tripwise.core.database.entity.ChatConversationEntity
import com.tripwise.core.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ChatDaoTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val t0 = Instant.parse("2026-03-01T10:00:00Z")

    private fun conversation(id: String, at: Instant) = ChatConversationEntity(id, "Chat $id", "jaipur", null, "MID", 3, 2, "FOOD,CULTURE", at, at)
    private fun message(id: String, conv: String, text: String, at: Instant) = ChatMessageEntity(id, conv, "USER", text, null, at)

    @Test fun `history is newest first with a preview, and deleting cascades to messages`() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, TripWiseDatabase::class.java).allowMainThreadQueries().build()
        val dao = db.chatDao()
        dao.upsertConversation(conversation("a", t0))
        dao.upsertConversation(conversation("b", t0.plusSeconds(10)))
        dao.saveMessage(message("m1", "a", "Hotels in Jaipur?", t0.plusSeconds(20)), t0.plusSeconds(20))
        dao.saveMessage(message("m2", "a", "", t0.plusSeconds(30)), t0.plusSeconds(30)) // empty reply never becomes the preview

        val list = dao.observeConversations().first()
        assertEquals(listOf("a", "b"), list.map { it.conversation.id })
        assertEquals("Hotels in Jaipur?", list.first().preview)
        assertEquals(listOf("m1", "m2"), dao.messages("a").map { it.id })

        dao.deleteConversation("a")
        assertTrue(dao.messages("a").isEmpty())
        assertEquals(listOf("b"), dao.observeConversations().first().map { it.conversation.id })
        db.close()
    }

    @Test fun `migration 1 to 2 creates the chat tables Room expects and keeps trips`() = runTest {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        // Build the current schema, then turn it back into a v1 file (no chat tables, user_version 1).
        Room.databaseBuilder(context, TripWiseDatabase::class.java, name).build().apply { openHelper.writableDatabase; close() }
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { raw ->
            raw.execSQL("DROP TABLE chat_messages")
            raw.execSQL("DROP TABLE chat_conversations")
            raw.execSQL("INSERT INTO trips (id, title, destination_id, destination_name, cover_image_url, start_date, end_date, travellers, budget_minor, budget_currency, notes, created_at, updated_at, sync_state, server_version, deleted) VALUES ('t', 'Goa', 'goa', 'Goa', NULL, '2026-12-01', '2026-12-03', 2, NULL, NULL, NULL, 0, 0, 'PENDING', NULL, 0)")
            raw.version = 1
        }
        // Room validates every table against the entities after migrating; a mismatch throws here.
        val db = Room.databaseBuilder(context, TripWiseDatabase::class.java, name).addMigrations(*ALL_MIGRATIONS).allowMainThreadQueries().build()
        assertEquals("Goa", db.tripDao().observeById("t").first()?.title)
        db.chatDao().upsertConversation(conversation("c", t0))
        assertEquals(1, db.chatDao().observeConversations().first().size)
        db.close()
        context.deleteDatabase(name)
    }
}
