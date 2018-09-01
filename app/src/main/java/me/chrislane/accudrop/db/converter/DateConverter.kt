package me.chrislane.accudrop.db.converter

import android.arch.persistence.room.TypeConverter
import java.util.*

class DateConverter {
    /**
     * Convert a timestamp into a `Date` object.
     *
     * @param timestamp The timestamp to convert.
     * @return The matching `Date` object.
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    /**
     * Convert a `Date` object into a timestamp.
     *
     * @param date The date to convert.
     * @return The matching timestamp.
     */
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
