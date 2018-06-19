package me.chrislane.accudrop.db.converter;

import android.arch.persistence.room.TypeConverter;

import me.chrislane.accudrop.db.FallType;

public class FallTypeConverter {

    @TypeConverter
    public static FallType toFallType(String falltype) {
        return FallType.valueOf(falltype);
    }

    @TypeConverter
    public static String toString(FallType falltype) {
        return falltype.name();
    }
}
