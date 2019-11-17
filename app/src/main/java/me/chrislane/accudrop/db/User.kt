package me.chrislane.accudrop.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user")
class User {

    @PrimaryKey
    var uuid: UUID = UUID.fromString("")

    var name: String? = null
}
