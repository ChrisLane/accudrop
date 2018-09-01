package me.chrislane.accudrop.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "jump")
class Jump {
    @PrimaryKey
    var id: Int = 0

    var time: Date? = null
}
