package me.chrislane.accudrop.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

import me.chrislane.accudrop.db.converter.DateConverter
import me.chrislane.accudrop.db.converter.FallTypeConverter
import me.chrislane.accudrop.db.converter.UuidConverter

@Database(entities = [Jump::class, Position::class, User::class], version = 7)
@TypeConverters(DateConverter::class, UuidConverter::class, FallTypeConverter::class)
abstract class AccudropDb : RoomDatabase() {

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

        private var INSTANCE: AccudropDb? = null
        private val DB_NAME = "accudrop"

        /**
         * Get an instance of the database.
         *
         * @param context A context in the application.
         * @return An instance of an `AccudropDb` database.
         */
        fun getDatabase(context: Context): AccudropDb {

            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder<AccudropDb>(context.applicationContext, AccudropDb::class.java, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return INSTANCE as AccudropDb
        }

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