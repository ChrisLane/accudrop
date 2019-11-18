package me.chrislane.accudrop.util

import android.content.Context
import java.util.*

class UserUtil {
  companion object {
    fun getCurrentUserUuid(context: Context): UUID {
      val userPrefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
      return UUID.fromString(userPrefs.getString("userUUID", ""))
    }
  }
}