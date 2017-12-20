package me.chrislane.accudrop.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "jump")
public class Jump {
    @PrimaryKey
    public int id;

    public Date time;
}
