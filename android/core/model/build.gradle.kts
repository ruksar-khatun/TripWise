plugins {
    alias(libs.plugins.tripwise.jvm.library)
}
// Domain models are plain immutable Kotlin. No serialization annotations here:
// DTOs (network) and entities (database) are mapped to/from these in :core:data.
