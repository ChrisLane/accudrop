package me.chrislane.accudrop.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName = "user")
public class User {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    public String name;
}
