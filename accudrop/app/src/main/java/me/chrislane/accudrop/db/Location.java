package me.chrislane.accudrop.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;

@Entity(foreignKeys = {
        @ForeignKey(entity = Jump.class,
                parentColumns = "id",
                childColumns = "jump_id")
})
public class Location {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int altitude;

    private LatLng coordinate;

    private Timestamp time;

    @ColumnInfo(name = "jump_id")
    private int jumpId;
}