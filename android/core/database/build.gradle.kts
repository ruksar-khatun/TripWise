plugins {
    alias(libs.plugins.tripwise.android.library)
    alias(libs.plugins.tripwise.android.room)
    alias(libs.plugins.tripwise.hilt)
}

android {
    namespace = "com.tripwise.core.database"
}

// The @Database class, entities and DAOs are introduced with the first persisted feature
// (Room requires at least one entity). Schemas are exported to ./schemas for migration tests.
