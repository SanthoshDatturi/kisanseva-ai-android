package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.QueuedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueuedMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: QueuedMessageEntity)

    @Query("SELECT * FROM queued_messages ORDER BY timestamp ASC")
    fun getQueuedMessages(): Flow<List<QueuedMessageEntity>>

    @Query("DELETE FROM queued_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

}
