package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface JumpDao {

    /**
     * Find all jump records.
     *
     * @return A <code>LiveData</code> object containing a list of all jump records.
     */
    @Query("SELECT * FROM jump")
    LiveData<List<Jump>> findAllJumps();

    /**
     * Find a jump record from a jump ID.
     *
     * @param id The jump ID of the record.
     * @return A <code>LiveData</code> object containing the jump record.
     */
    @Query("SELECT * FROM jump " +
            "WHERE id = :id")
    LiveData<Jump> findJumpWithId(int id);

    /**
     * Find the last jump.
     *
     * @return A <code>LiveData</code> object containing the last jump.
     */
    @Query("SELECT * FROM jump " +
            "WHERE id IN ( " +
            "SELECT MAX(id) FROM jump ) ")
    LiveData<Jump> findLastJump();

    /**
     * Find the last jump ID.
     *
     * @return A <code>LiveData</code> object containing the last jump ID.
     */
    @Query("SELECT MAX(id) FROM jump")
    LiveData<Integer> findLastJumpId();

    /**
     * Get the last jump ID.
     *
     * @return The last jump ID.
     */
    @Query("SELECT MAX(id) FROM jump")
    Integer getLastJumpId();

    @Query("SELECT EXISTS(SELECT * FROM jump " +
            "WHERE id = :id)")
    Boolean jumpExists(int id);

    /**
     * Insert a jump.
     *
     * @param jump The jump to insert.
     */
    @Insert
    void insertJump(Jump jump);

    /**
     * Delete all jump records.
     */
    @Query("DELETE FROM jump")
    void deleteAll();
}
