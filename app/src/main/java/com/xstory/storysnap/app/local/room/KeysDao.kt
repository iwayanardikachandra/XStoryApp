package com.xstory.storysnap.app.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xstory.storysnap.app.local.entity.Keys

@Dao
interface KeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keysKeys: List<Keys>)

    @Query("SELECT * FROM remote_keys WHERE id = :id")
    suspend fun getRemoteKeysId(id: String): Keys?

    @Query("DELETE FROM remote_keys")
    suspend fun deleteRemoteKeys()
}