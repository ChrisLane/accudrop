package me.chrislane.accudrop.db

import android.arch.persistence.room.*
import java.util.*

@Entity(tableName = "position", indices = arrayOf(Index("jump_id")), foreignKeys = arrayOf(ForeignKey(entity = Jump::class, parentColumns = arrayOf("id"), childColumns = arrayOf("jump_id"))))
class Position {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var useruuid: String? = null

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