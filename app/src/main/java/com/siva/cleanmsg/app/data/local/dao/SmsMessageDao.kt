package com.siva.cleanmsg.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

data class SmsIdAndCategory(val smsId: Long, val category: String, val isManuallyClassified: Boolean)

data class CategoryCount(val category: String, val count: Int)

@Dao
interface SmsMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SmsMessageEntity>)

    @Update
    suspend fun updateAll(messages: List<SmsMessageEntity>)

    @Query("SELECT * FROM messages ORDER BY date DESC")
    fun observeAll(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM messages WHERE category = :category ORDER BY date DESC")
    fun observeByCategory(category: String): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM messages WHERE smsId = :smsId")
    fun observeById(smsId: Long): Flow<SmsMessageEntity?>

    @Query("SELECT * FROM messages")
    suspend fun getAllOnce(): List<SmsMessageEntity>

    @Query("SELECT smsId, category, isManuallyClassified FROM messages")
    suspend fun getAllCategoriesBySmsId(): List<SmsIdAndCategory>

    @Query("SELECT COUNT(*) FROM messages")
    fun observeCount(): Flow<Int>

    @Query("SELECT category, COUNT(*) as count FROM messages GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>

    @Query("UPDATE messages SET category = :category, isManuallyClassified = 1 WHERE smsId = :smsId")
    suspend fun setCategoryManually(smsId: Long, category: String)

    @Query("DELETE FROM messages WHERE smsId IN (:smsIds)")
    suspend fun deleteByIds(smsIds: List<Long>)
}
