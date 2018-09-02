package me.chrislane.accudrop.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "user")
class User {

    @PrimaryKey
    var uuid: UUID = UUID.fromString("")

    var name: String? = null
}
