package me.chrislane.accudrop.db.converter

import android.arch.persistence.room.TypeConverter

import me.chrislane.accudrop.db.FallType

class FallTypeConverter {

    @TypeConverter
    fun toFallType(fallType: String): FallType {
        return FallType.valueOf(fallType)
    }

    @TypeConverter
    fun toString(fallType: FallType): String {
        return fallType.name
    }
}
