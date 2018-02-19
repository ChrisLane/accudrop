package me.chrislane.accudrop.db.converter;

import android.arch.persistence.room.TypeConverter;

import java.util.UUID;

public class UuidConverter {
    @TypeConverter
    public static UUID toUuid(String uuidString) {
        return UUID.fromString(uuidString);
    }

    @TypeConverter
    public static String toStr(UUID uuid) {
        return uuid.toString();
    }
}
