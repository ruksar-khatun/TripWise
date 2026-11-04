package com.tripwise.core.database.entity

/**
 * Every user-owned row carries sync metadata from day one, so the sync phase needs no data migration:
 * new/edited rows are PENDING until the server acknowledges them; deletions are soft (deleted = 1).
 */
enum class SyncState { PENDING, SYNCED }
