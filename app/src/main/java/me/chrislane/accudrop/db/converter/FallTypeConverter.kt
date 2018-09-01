package me.chrislane.accudrop.db.converter

import android.arch.persistence.room.TypeConverter

import me.chrislane.accudrop.db.FallType

class FallTypeConverter {

    @TypeConverter
    fun toFallType(falltype: String): FallType {
        return FallType.valueOf(falltype)
    }

    @TypeConverter
    fun toString(falltype: FallType): String {
        return falltype.name
    }
}
