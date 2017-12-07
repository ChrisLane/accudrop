package me.chrislane.accudrop.db;

import android.arch.persistence.room.*;

import java.util.Date;

@Entity(
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

    public int altitude;

    public double latitude;

    public double longitude;

    public Date time;

    @ColumnInfo(name = "jump_id")
    public int jumpId;
}