package com.siva.cleanmsg.app.repository

import com.siva.cleanmsg.app.data.classification.CategoryInfo
import com.siva.cleanmsg.app.data.classification.MatchType
import com.siva.cleanmsg.app.data.local.dao.CategoryCount
import com.siva.cleanmsg.app.data.local.entity.CategoryEntity
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity
import com.siva.cleanmsg.app.data.local.entity.UserRuleEntity
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    /** Reads every message from content://sms and upserts it into the local cache. */
    suspend fun scanAndCacheInbox()

    /** Re-tags cached messages against the current rules without touching the SMS provider. */
    suspend fun classifyAllMessages()

    fun observeMessages(): Flow<List<SmsMessageEntity>>

    fun observeMessagesByCategory(categoryKey: String): Flow<List<SmsMessageEntity>>

    fun observeMessage(smsId: Long): Flow<SmsMessageEntity?>

    /** Manual override from the Message Detail screen; sticks across future classification runs. */
    suspend fun reclassifyMessage(smsId: Long, newCategoryKey: String)

    /** Deletes from content://sms (requires default-SMS-app role) and drops the local cache rows. */
    suspend fun deleteMessages(smsIds: Set<Long>)

    fun observeMessageCount(): Flow<Int>

    fun observeCategoryCounts(): Flow<List<CategoryCount>>

    /** Built-in categories plus any user-added ones, merged into one list. */
    fun observeAllCategories(): Flow<List<CategoryInfo>>

    suspend fun getCategory(key: String): CategoryEntity?

    /** Returns the new category's generated key. */
    suspend fun addCategory(name: String, colorHex: String?): String

    suspend fun updateCategory(key: String, name: String, colorHex: String?)

    /** Reassigns any of its messages to Uncategorized and drops any rule that targeted it. */
    suspend fun deleteCategory(key: String)

    fun observeRules(): Flow<List<UserRuleEntity>>

    suspend fun addRule(matchType: MatchType, matchValue: String, targetCategoryKey: String)

    suspend fun updateRule(rule: UserRuleEntity)

    suspend fun deleteRule(rule: UserRuleEntity)
}
