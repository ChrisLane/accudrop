package me.chrislane.accudrop.db

import androidx.room.*
import java.util.*

@Entity(
    tableName = "position",
    indices = [Index("jump_id")],
    foreignKeys = [
      ForeignKey(entity = Jump::class, parentColumns = ["id"], childColumns = ["jump_id"]),
      ForeignKey(entity = User::class, parentColumns = ["uuid"], childColumns = ["user_id"])])
data class Position(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "jump_id")
    val jumpId: Int,
    @ColumnInfo(name = "user_id")
    val userUuid: UUID,
    val time: Date,
    val altitude: Int?,
    @ColumnInfo(name = "vertical_speed")
    val vSpeed: Double?,
    @ColumnInfo(name = "horizontal_speed")
    val hSpeed: Float?,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "fall_type")
    val fallType: FallType)