package me.chrislane.accudrop.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "jump")
data class Jump(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: Date)
