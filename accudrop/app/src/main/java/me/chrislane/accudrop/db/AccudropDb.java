package me.chrislane.accudrop.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Jump.class, Position.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AccudropDb extends RoomDatabase {

    private static AccudropDb INSTANCE;

    public abstract JumpDao jumpModel();

    public abstract PositionDao locationModel();

    public static AccudropDb getInMemoryDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AccudropDb.class).build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}