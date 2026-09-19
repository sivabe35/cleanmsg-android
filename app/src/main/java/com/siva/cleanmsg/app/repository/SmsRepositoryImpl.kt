package com.siva.cleanmsg.app.repository

import android.content.Context
import android.provider.Telephony
import com.siva.cleanmsg.app.data.classification.BuiltInCategory
import com.siva.cleanmsg.app.data.classification.CategoryInfo
import com.siva.cleanmsg.app.data.classification.ClassificationEngine
import com.siva.cleanmsg.app.data.classification.ClassificationRule
import com.siva.cleanmsg.app.data.classification.MatchType
import com.siva.cleanmsg.app.data.local.dao.CategoryCount
import com.siva.cleanmsg.app.data.local.dao.CategoryDao
import com.siva.cleanmsg.app.data.local.dao.SmsIdAndCategory
import com.siva.cleanmsg.app.data.local.dao.SmsMessageDao
import com.siva.cleanmsg.app.data.local.dao.UserRuleDao
import com.siva.cleanmsg.app.data.local.entity.CategoryEntity
import com.siva.cleanmsg.app.data.local.entity.SmsMessageEntity
import com.siva.cleanmsg.app.data.local.entity.UserRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class SmsRepositoryImpl(
    private val context: Context,
    private val smsMessageDao: SmsMessageDao,
    private val userRuleDao: UserRuleDao,
    private val categoryDao: CategoryDao,
    private val classificationEngine: ClassificationEngine
) : SmsRepository {

    override suspend fun scanAndCacheInbox() = withContext(Dispatchers.IO) {
        // Preserve any category/manual-override already on a message so re-scanning
        // the provider doesn't wipe out prior classification work (PRD section 4).
        val existingBySmsId = smsMessageDao.getAllCategoriesBySmsId().associateBy { it.smsId }
        smsMessageDao.insertAll(readAllMessagesFromProvider(existingBySmsId))
    }

    override suspend fun classifyAllMessages() = withContext(Dispatchers.IO) {
        val userRules = userRuleDao.getAllOnce().mapNotNull { it.toClassificationRuleOrNull() }
        val changed = smsMessageDao.getAllOnce().mapNotNull { message ->
            if (message.isManuallyClassified) return@mapNotNull null
            val newCategory = classificationEngine.classify(message.address, message.body, userRules)
            if (newCategory == message.category) null else message.copy(category = newCategory)
        }
        if (changed.isNotEmpty()) smsMessageDao.updateAll(changed)
    }

    override fun observeMessages(): Flow<List<SmsMessageEntity>> = smsMessageDao.observeAll()

    override fun observeMessagesByCategory(categoryKey: String): Flow<List<SmsMessageEntity>> =
        smsMessageDao.observeByCategory(categoryKey)

    override fun observeMessage(smsId: Long): Flow<SmsMessageEntity?> = smsMessageDao.observeById(smsId)

    override suspend fun reclassifyMessage(smsId: Long, newCategoryKey: String) = withContext(Dispatchers.IO) {
        smsMessageDao.setCategoryManually(smsId, newCategoryKey)
    }

    override suspend fun deleteMessages(smsIds: Set<Long>) = withContext(Dispatchers.IO) {
        if (smsIds.isEmpty()) return@withContext

        val placeholders = smsIds.joinToString(",") { "?" }
        val selection = "${Telephony.Sms._ID} IN ($placeholders)"
        val selectionArgs = smsIds.map { it.toString() }.toTypedArray()
        context.contentResolver.delete(Telephony.Sms.CONTENT_URI, selection, selectionArgs)

        smsMessageDao.deleteByIds(smsIds.toList())
    }

    override fun observeMessageCount(): Flow<Int> = smsMessageDao.observeCount()

    override fun observeCategoryCounts(): Flow<List<CategoryCount>> = smsMessageDao.observeCategoryCounts()

    override fun observeAllCategories(): Flow<List<CategoryInfo>> =
        categoryDao.observeAll().map { customCategories ->
            BuiltInCategory.entries.map { CategoryInfo(it.key, it.displayName, colorHex = null, isBuiltIn = true) } +
                customCategories.map { CategoryInfo(it.key, it.name, it.colorHex, isBuiltIn = false) }
        }

    override suspend fun getCategory(key: String): CategoryEntity? = categoryDao.getByKey(key)

    override suspend fun addCategory(name: String, colorHex: String?): String = withContext(Dispatchers.IO) {
        val key = "CUSTOM_${UUID.randomUUID()}"
        categoryDao.insert(CategoryEntity(key = key, name = name, colorHex = colorHex))
        key
    }

    override suspend fun updateCategory(key: String, name: String, colorHex: String?) = withContext(Dispatchers.IO) {
        categoryDao.update(CategoryEntity(key = key, name = name, colorHex = colorHex))
    }

    override suspend fun deleteCategory(key: String) = withContext(Dispatchers.IO) {
        val affectedMessages = smsMessageDao.getAllOnce().filter { it.category == key }
            .map { it.copy(category = BuiltInCategory.UNCATEGORIZED.key, isManuallyClassified = false) }
        if (affectedMessages.isNotEmpty()) smsMessageDao.updateAll(affectedMessages)

        userRuleDao.getAllOnce().filter { it.targetCategoryKey == key }.forEach { userRuleDao.delete(it) }

        categoryDao.deleteByKey(key)
    }

    override fun observeRules(): Flow<List<UserRuleEntity>> = userRuleDao.observeAll()

    override suspend fun addRule(matchType: MatchType, matchValue: String, targetCategoryKey: String) {
        withContext(Dispatchers.IO) {
            userRuleDao.insert(
                UserRuleEntity(matchType = matchType.name, matchValue = matchValue, targetCategoryKey = targetCategoryKey)
            )
        }
    }

    override suspend fun updateRule(rule: UserRuleEntity) = withContext(Dispatchers.IO) {
        userRuleDao.update(rule)
    }

    override suspend fun deleteRule(rule: UserRuleEntity) = withContext(Dispatchers.IO) {
        userRuleDao.delete(rule)
    }

    private fun readAllMessagesFromProvider(existingBySmsId: Map<Long, SmsIdAndCategory>): List<SmsMessageEntity> {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val messages = mutableListOf<SmsMessageEntity>()
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

            while (cursor.moveToNext()) {
                val smsId = cursor.getLong(idIndex)
                val existing = existingBySmsId[smsId]
                messages.add(
                    SmsMessageEntity(
                        smsId = smsId,
                        address = cursor.getString(addressIndex).orEmpty(),
                        body = cursor.getString(bodyIndex).orEmpty(),
                        date = cursor.getLong(dateIndex),
                        type = cursor.getInt(typeIndex),
                        category = existing?.category ?: BuiltInCategory.UNCATEGORIZED.key,
                        isManuallyClassified = existing?.isManuallyClassified ?: false
                    )
                )
            }
        }
        return messages
    }
}

private fun UserRuleEntity.toClassificationRuleOrNull(): ClassificationRule? =
    try {
        ClassificationRule(
            categoryKey = targetCategoryKey,
            matchType = MatchType.valueOf(matchType),
            values = listOf(matchValue)
        )
    } catch (e: IllegalArgumentException) {
        null
    }
