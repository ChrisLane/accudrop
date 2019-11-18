package me.chrislane.accudrop.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JumpDao {

    /**
     * Get the last jump ID.
     *
     * @return The last jump ID.
     */
    @get:Query("SELECT MAX(id) FROM jump")
    val lastJumpId: Int?

    /**
     * Get the first jump ID.
     *
     * @return The first jump ID.
     */
    @get:Query("SELECT MIN(id) FROM jump")
    val firstJumpId: Int?

    /**
     * Find all jump records.
     *
     * @return A `LiveData` object containing a list of all jump records.
     */
    @Query("SELECT * FROM jump")
    fun findAllJumps(): LiveData<MutableList<Jump>>

    /**
     * Find a jump record from a jump ID.
     *
     * @param id The jump ID of the record.
     * @return A `LiveData` object containing the jump record.
     */
    @Query("SELECT * FROM jump WHERE id = :id")
    fun findJumpWithId(id: Int): LiveData<Jump>

    /**
     * Find the last jump.
     *
     * @return A `LiveData` object containing the last jump.
     */
    @Query("SELECT * FROM jump WHERE id IN (SELECT MAX(id) FROM jump) ")
    fun findLastJump(): LiveData<Jump>

    /**
     * Find the first jump ID.
     *
     * @return A `LiveData` object containing the first jump ID.
     */
    @Query("SELECT MIN(id) FROM jump")
    fun findFirstJumpId(): LiveData<Int>

    /**
     * Find the last jump ID.
     *
     * @return A `LiveData` object containing the last jump ID.
     */
    @Query("SELECT MAX(id) FROM jump")
    fun findLastJumpId(): LiveData<Int>

    @Query("SELECT MAX(id) FROM jump")
    suspend fun getLastJumpId(): Int?

    @Query("SELECT EXISTS(SELECT * FROM jump WHERE id = :id)")
    fun jumpExists(id: Int): Boolean?

    /**
     * Insert a jump.
     *
     * @param jump The jump to insert.
     */
    @Insert
    suspend fun insertJump(jump: Jump)

    /**
     * Delete all jump records.
     */
    @Query("DELETE FROM jump")
    fun deleteAll()

    @Query("DELETE FROM jump " + "WHERE id = :jumpId")
    fun deleteJump(jumpId: Int)
}
