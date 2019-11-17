package me.chrislane.accudrop.db

import androidx.room.*
import java.util.*

@Entity(tableName = "position", indices = [Index("jump_id")],
        foreignKeys = [ForeignKey(entity = Jump::class, parentColumns = arrayOf("id"), childColumns = arrayOf("jump_id"))])
class Position {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var altitude: Int? = null

    var vspeed: Double? = null

    var hspeed: Float? = null

    var latitude: Double? = null

    var longitude: Double? = null

    var time: Date? = null

    var fallType: FallType? = null

    @ColumnInfo(name = "jump_id")
    var jumpId: Int = 0
}