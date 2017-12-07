package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface JumpDao {

    @Query("SELECT * FROM Jump")
    LiveData<List<Jump>> findAllJumps();

    @Query("SELECT * FROM Jump " +
    "WHERE id = :id ")
    LiveData<Jump> findJumpWithId(int id);

    @Query("SELECT * FROM Jump " +
    "WHERE id IN ( " +
    "SELECT MAX(id) FROM Jump ) ")
    LiveData<Jump> findLastJump();

    @Query("SELECT MAX(id) FROM Jump")
    LiveData<Integer> findLastJumpId();

    @Insert
    void insertJump(Jump jump);

    @Query("DELETE FROM Jump")
    void deleteAll();
}
