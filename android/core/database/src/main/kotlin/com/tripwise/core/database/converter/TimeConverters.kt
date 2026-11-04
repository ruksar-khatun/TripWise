package com.tripwise.core.database.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Shared Room converters. Storage formats:
 *  - Instant   → epoch millis (Long)
 *  - LocalDate → ISO-8601 "yyyy-MM-dd" (sortable as text)
 *  - LocalTime → ISO-8601 "HH:mm[:ss]"
 * Money will be stored as two columns (amountMinor: Long, currency: String), never as a floating-point value.
 */
class TimeConverters {
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter fun localDateToString(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter fun localTimeToString(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
