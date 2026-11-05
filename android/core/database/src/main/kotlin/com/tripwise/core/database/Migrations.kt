package com.tripwise.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v2 adds TripMate AI chat history. SQL mirrors the Room-generated schema (validated by MigrationTest). */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `chat_conversations` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`destination_id` TEXT, `trip_id` TEXT, `budget_level` TEXT, `days` INTEGER, `travellers` INTEGER, " +
                "`interests` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_conversations_updated_at` ON `chat_conversations` (`updated_at`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `conversation_id` TEXT NOT NULL, " +
                "`role` TEXT NOT NULL, `text` TEXT NOT NULL, `places_json` TEXT, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`conversation_id`) REFERENCES `chat_conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_conversation_id` ON `chat_messages` (`conversation_id`)")
    }
}

internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
