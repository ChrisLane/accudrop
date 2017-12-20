package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface JumpDao {

    @Query("SELECT * FROM jump")
    LiveData<List<Jump>> findAllJumps();

    @Query("SELECT * FROM jump " +
            "WHERE id = :id ")
    LiveData<Jump> findJumpWithId(int id);

    @Query("SELECT * FROM jump " +
            "WHERE id IN ( " +
            "SELECT MAX(id) FROM jump ) ")
    LiveData<Jump> findLastJump();

    @Query("SELECT CAST(MAX(id) AS INTEGER) FROM jump")
    LiveData<Integer> findLastJumpId();

    @Insert
    void insertJump(Jump jump);

    @Query("DELETE FROM jump")
    void deleteAll();
}
