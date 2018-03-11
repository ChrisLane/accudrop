package me.chrislane.accudrop.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

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

    @NonNull
    public String useruuid;

    public Integer altitude;

    public Double vspeed;

    public Float hspeed;

    public Double latitude;

    public Double longitude;

    public Date time;

    public FallType fallType;

    @ColumnInfo(name = "jump_id")
    @NonNull
    public int jumpId;
}