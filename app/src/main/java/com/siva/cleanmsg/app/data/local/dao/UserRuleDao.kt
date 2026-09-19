package com.siva.cleanmsg.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.siva.cleanmsg.app.data.local.entity.UserRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRuleDao {
    @Query("SELECT * FROM user_rules ORDER BY id")
    fun observeAll(): Flow<List<UserRuleEntity>>

    @Query("SELECT * FROM user_rules ORDER BY id")
    suspend fun getAllOnce(): List<UserRuleEntity>

    @Insert
    suspend fun insert(rule: UserRuleEntity): Long

    @Update
    suspend fun update(rule: UserRuleEntity)

    @Delete
    suspend fun delete(rule: UserRuleEntity)
}
