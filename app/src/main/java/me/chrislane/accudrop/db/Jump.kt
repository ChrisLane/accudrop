package me.chrislane.accudrop.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "jump")
class Jump {
    @PrimaryKey
    var id: Int = 0

    var time: Date? = null
}
