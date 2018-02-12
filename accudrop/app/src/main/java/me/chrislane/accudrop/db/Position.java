package me.chrislane.accudrop.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "position",
        indices = {
                @Index("jump_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Jump.class,
                        parentColumns = "id",
                        childColumns = "jump_id")
        }
)
public class Position {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String useruuid;

    public int altitude;

    public float vspeed;

    public float hspeed;

    public double latitude;

    public double longitude;

    public Date time;

    @ColumnInfo(name = "jump_id")
    public int jumpId;
}