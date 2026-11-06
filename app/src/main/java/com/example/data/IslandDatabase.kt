package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "island_presets")
data class IslandPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val encryptedPayloadBase64: String,
    val checksumSha256: String,
    val resolutionTag: String,
    val isActive: Boolean = false,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugin_rules")
data class PluginRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val packageIdentifier: String,
    val triggerType: String,
    val triggerCondition: String,
    val targetModule: String,
    val encryptedActionPayloadBase64: String,
    val accentHex: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0,
    val lastTriggeredEpochMs: Long? = null
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val entitySummary: String,
    val encryptedSnapshotBase64: String,
    val checksumSha256: String,
    val resolutionProfile: String,
    val status: String = "PENDING", // PENDING or SYNCED
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val syncedAtEpochMs: Long? = null
)

@Dao
interface IslandDao {
    @Query("SELECT * FROM island_presets ORDER BY updatedAtEpochMs DESC")
    fun observeAllPresets(): Flow<List<IslandPresetEntity>>

    @Query("SELECT * FROM island_presets ORDER BY updatedAtEpochMs DESC")
    suspend fun getAllPresetsOnce(): List<IslandPresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: IslandPresetEntity): Long

    @Query("DELETE FROM island_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)

    @Query("UPDATE island_presets SET isActive = (id = :activeId)")
    suspend fun markPresetActive(activeId: Long)

    @Query("SELECT * FROM plugin_rules ORDER BY id DESC")
    fun observeAllPlugins(): Flow<List<PluginRuleEntity>>

    @Query("SELECT * FROM plugin_rules ORDER BY id DESC")
    suspend fun getAllPluginsOnce(): List<PluginRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: PluginRuleEntity): Long

    @Update
    suspend fun updatePlugin(plugin: PluginRuleEntity)

    @Query("DELETE FROM plugin_rules WHERE id = :id")
    suspend fun deletePluginById(id: Long)

    @Query("SELECT * FROM sync_queue ORDER BY createdAtEpochMs DESC")
    fun observeSyncQueue(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncRecord(record: SyncQueueEntity): Long

    @Query("UPDATE sync_queue SET status = 'SYNCED', syncedAtEpochMs = :syncedAt WHERE status = 'PENDING'")
    suspend fun markAllPendingSynced(syncedAt: Long): Int

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSyncedHistory()
}

@Database(
    entities = [
        IslandPresetEntity::class,
        PluginRuleEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IslandDatabase : RoomDatabase() {
    abstract fun islandDao(): IslandDao

    companion object {
        @Volatile
        private var INSTANCE: IslandDatabase? = null

        fun getInstance(context: Context): IslandDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    IslandDatabase::class.java,
                    "islestudio_encrypted_vault.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
