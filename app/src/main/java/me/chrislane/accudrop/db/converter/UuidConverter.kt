package me.chrislane.accudrop.db.converter

import androidx.room.TypeConverter
import java.util.*

class UuidConverter {

    @TypeConverter
    fun toUuid(uuidString: String): UUID {
        return UUID.fromString(uuidString)
    }

    @TypeConverter
    fun toStr(uuid: UUID): String {
        return uuid.toString()
    }
}
