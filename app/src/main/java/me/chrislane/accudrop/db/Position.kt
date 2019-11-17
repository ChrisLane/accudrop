package me.chrislane.accudrop.db

import androidx.room.*
import java.util.*

@Entity(
    tableName = "position",
    indices = [Index("jump_id", "user_uuid")],
    foreignKeys = [
      ForeignKey(entity = Jump::class, parentColumns = arrayOf("id"), childColumns = arrayOf("jump_id")),
      ForeignKey(entity = User::class, parentColumns = arrayOf("uuid"), childColumns = arrayOf("user_uuid"))])
data class Position(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "jump_id")
    val jumpId: Int = 0,
    @ColumnInfo(name = "user_uuid")
    val userUuid: UUID,
    val altitude: Int,
    @ColumnInfo(name = "vertical_speed")
    val vSpeed: Double,
    @ColumnInfo(name = "horizontal_speed")
    val hSpeed: Float,
    val latitude: Double,
    val longitude: Double,
    val time: Date,
    @ColumnInfo(name = "fall_type")
    val fallType: FallType)