package me.chrislane.accudrop.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.chrislane.accudrop.db.converter.DateConverter
import me.chrislane.accudrop.db.converter.FallTypeConverter
import me.chrislane.accudrop.db.converter.UuidConverter

@Database(entities = [Jump::class, Position::class, User::class], version = 11)
@TypeConverters(DateConverter::class, UuidConverter::class, FallTypeConverter::class)
abstract class AccuDropDb : RoomDatabase() {

  /**
   * Get a data access object for the `jump` table.
   *
   * @return A data access object for the `jump` table.
   */
  abstract fun jumpModel(): JumpDao

  /**
   * Get a data access object for the `position` table.
   *
   * @return A data access object for the `position` table.
   */
  abstract fun locationModel(): PositionDao

  abstract fun userModel(): UserDao

  companion object {

    @Volatile
    private var INSTANCE: AccuDropDb? = null
    private const val DB_NAME = "accudrop.db"

    /**
     * Get an instance of the database.
     *
     * @param context A context in the application.
     * @return An instance of an `AccuDropDb` database.
     */
    fun getInstance(context: Context): AccuDropDb =
        INSTANCE ?: synchronized(this) {
          INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

    private fun buildDatabase(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            AccuDropDb::class.java, DB_NAME)
            .build()


    /**
     * Remove the database instance.
     */
    fun destroyInstance() {
      INSTANCE = null
    }

    fun clearDatabase(context: Context) {
      context.deleteDatabase(DB_NAME)
    }
  }
}