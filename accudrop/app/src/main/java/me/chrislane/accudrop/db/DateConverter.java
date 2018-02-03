package me.chrislane.accudrop.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    /**
     * Convert a timestamp into a <code>Date</code> object.
     *
     * @param timestamp The timestamp to convert.
     * @return The matching <code>Date</code> object.
     */
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    /**
     * Convert a <code>Date</code> object into a timestamp.
     *
     * @param date The date to convert.
     * @return The matching timestamp.
     */
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
