package me.chrislane.accudrop.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.chrislane.accudrop.db.converter.DateConverter
import me.chrislane.accudrop.db.converter.FallTypeConverter

@Database(entities = [Jump::class, Position::class], version = 9)
@TypeConverters(DateConverter::class, FallTypeConverter::class)
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

    companion object {

        private var instance: AccuDropDb? = null
        private const val DB_NAME = "accudrop"

        /**
         * Get an instance of the database.
         *
         * @param context A context in the application.
         * @return An instance of an `AccuDropDb` database.
         */
        fun getDatabase(context: Context): AccuDropDb {

            if (instance == null) {
                instance = Room.databaseBuilder<AccuDropDb>(context.applicationContext, AccuDropDb::class.java, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance as AccuDropDb
        }

        /**
         * Remove the database instance.
         */
        fun destroyInstance() {
            instance = null
        }

        fun clearDatabase(context: Context) {
            context.deleteDatabase(DB_NAME)
        }
    }
}