package me.chrislane.accudrop.db;

import android.arch.persistence.room.PrimaryKey;

import java.sql.Timestamp;

public class Jump {
    @PrimaryKey
    private int id;

    private Timestamp time;
}
