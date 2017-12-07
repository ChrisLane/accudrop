package me.chrislane.accudrop.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.location.Location;

@Database(entities = {Jump.class, Location.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AccudropDb extends RoomDatabase {

    public abstract JumpDao jumpModel();

    public abstract LocationDao locationModel();
}