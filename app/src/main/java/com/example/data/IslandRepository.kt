package com.example.data

import com.example.model.IslandConfiguration
import com.example.model.IslandModuleType
import com.example.model.NotchAnchorMode
import com.example.model.PluginRuleModel
import com.example.model.PluginTriggerType
import com.example.model.ResolutionScalePreset
import com.example.model.SyncQueueRecord
import com.example.model.ThemePreference
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

data class SavedIslandPreset(
    val id: Long,
    val name: String,
    val config: IslandConfiguration,
    val checksumSha256: String,
    val resolutionTag: String,
    val isActive: Boolean,
    val updatedAtEpochMs: Long
)

class IslandRepository(
    private val dao: IslandDao,
    private val cryptoManager: CryptoManager = CryptoManager()
) {

    val presetsFlow: Flow<List<SavedIslandPreset>> = dao.observeAllPresets().map { list ->
        list.mapNotNull { entity ->
            runCatching {
                val plainJson = cryptoManager.decrypt(entity.encryptedPayloadBase64)
                val config = deserializeConfig(plainJson)
                SavedIslandPreset(
                    id = entity.id,
                    name = entity.name,
                    config = config,
                    checksumSha256 = entity.checksumSha256,
                    resolutionTag = entity.resolutionTag,
                    isActive = entity.isActive,
                    updatedAtEpochMs = entity.updatedAtEpochMs
                )
            }.getOrNull()
        }
    }

    val pluginsFlow: Flow<List<PluginRuleModel>> = dao.observeAllPlugins().map { list ->
        list.map { entity ->
            val decryptedObj = runCatching {
                JSONObject(cryptoManager.decrypt(entity.encryptedActionPayloadBase64))
            }.getOrElse { JSONObject() }

            PluginRuleModel(
                id = entity.id,
                name = entity.name,
                packageIdentifier = entity.packageIdentifier,
                triggerType = runCatching { PluginTriggerType.valueOf(entity.triggerType) }
                    .getOrDefault(PluginTriggerType.BROADCAST_INTENT),
                triggerCondition = entity.triggerCondition,
                targetModule = runCatching { IslandModuleType.valueOf(entity.targetModule) }
                    .getOrDefault(IslandModuleType.NOTIFICATION),
                headlineTemplate = decryptedObj.optString("headline", entity.name),
                detailTemplate = decryptedObj.optString("detail", entity.triggerCondition),
                accentHex = entity.accentHex,
                isEnabled = entity.isEnabled,
                executionCount = entity.executionCount,
                lastTriggeredEpochMs = entity.lastTriggeredEpochMs
            )
        }
    }

    val syncQueueFlow: Flow<List<SyncQueueRecord>> = dao.observeSyncQueue().map { list ->
        list.map { entity ->
            SyncQueueRecord(
                id = entity.id,
                operationType = entity.operationType,
                entitySummary = entity.entitySummary,
                checksumSha256 = entity.checksumSha256,
                resolutionProfile = entity.resolutionProfile,
                status = entity.status,
                createdAtEpochMs = entity.createdAtEpochMs,
                syncedAtEpochMs = entity.syncedAtEpochMs
            )
        }
    }

    suspend fun ensureSeeded() {
        val existingPresets = dao.getAllPresetsOnce()
        if (existingPresets.isEmpty()) {
            savePreset(
                config = IslandConfiguration(
                    presetName = "iOS 18 Pro Precision",
                    offsetXDp = 0f,
                    offsetYDp = 8f,
                    compactWidthDp = 186f,
                    compactHeightDp = 36f,
                    expandedWidthDp = 348f,
                    expandedHeightDp = 178f,
                    springStiffness = 520f,
                    springDamping = 0.68f,
                    accentColorHex = "#FA2D48",
                    anchorMode = NotchAnchorMode.CENTER
                ),
                markActive = true,
                recordSync = true
            )
            savePreset(
                config = IslandConfiguration(
                    presetName = "Left Punch-Hole Minimal",
                    offsetXDp = -92f,
                    offsetYDp = 6f,
                    compactWidthDp = 164f,
                    compactHeightDp = 34f,
                    expandedWidthDp = 336f,
                    expandedHeightDp = 168f,
                    springStiffness = 680f,
                    springDamping = 0.78f,
                    accentColorHex = "#34C759",
                    anchorMode = NotchAnchorMode.LEFT_PUNCH
                ),
                markActive = false,
                recordSync = false
            )
            savePreset(
                config = IslandConfiguration(
                    presetName = "macOS Notch Studio Dock",
                    offsetXDp = 0f,
                    offsetYDp = 0f,
                    compactWidthDp = 212f,
                    compactHeightDp = 38f,
                    expandedWidthDp = 364f,
                    expandedHeightDp = 188f,
                    cornerRadiusDp = 24f,
                    springStiffness = 440f,
                    springDamping = 0.62f,
                    accentColorHex = "#0A84FF",
                    anchorMode = NotchAnchorMode.CENTER
                ),
                markActive = false,
                recordSync = false
            )
        }

        val existingPlugins = dao.getAllPluginsOnce()
        if (existingPlugins.isEmpty()) {
            createPlugin(
                name = "Low Battery < 20% Guard",
                packageIdentifier = "com.android.systemui.battery",
                triggerType = PluginTriggerType.BATTERY_THRESHOLD,
                triggerCondition = "battery <= 20% || turbo_charge",
                targetModule = IslandModuleType.NOTIFICATION,
                headlineTemplate = "27W Fast Charge Connected",
                detailTemplate = "Battery at 84% • Full in 18m",
                accentHex = "#34C759",
                recordSync = false
            )
            createPlugin(
                name = "CI/CD Build Webhook",
                packageIdentifier = "io.github.actions.webhook",
                triggerType = PluginTriggerType.WEBHOOK_POLL,
                triggerCondition = "POST /v1/island/build-status",
                targetModule = IslandModuleType.TIMER,
                headlineTemplate = "Release Pipeline #409",
                detailTemplate = "Compiling APK • 01:14 remaining",
                accentHex = "#FF9F0A",
                recordSync = false
            )
            createPlugin(
                name = "Express Courier Live ETA",
                packageIdentifier = "com.islestudio.plugin.delivery",
                triggerType = PluginTriggerType.BROADCAST_INTENT,
                triggerCondition = "com.example.islestudio.ACTION_PUSH_ACTIVITY",
                targetModule = IslandModuleType.NAVIGATION,
                headlineTemplate = "Courier 3 Stops Away",
                detailTemplate = "Arriving at 420 Market St in 4 min",
                accentHex = "#0A84FF",
                recordSync = false
            )
        }
    }

    suspend fun savePreset(
        config: IslandConfiguration,
        markActive: Boolean = true,
        recordSync: Boolean = true
    ): Long {
        val json = serializeConfig(config)
        val encrypted = cryptoManager.encrypt(json)
        val checksum = cryptoManager.computeSha256(json)
        val id = dao.insertPreset(
            IslandPresetEntity(
                name = config.presetName,
                encryptedPayloadBase64 = encrypted,
                checksumSha256 = checksum,
                resolutionTag = config.resolutionPreset.label,
                isActive = markActive,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        if (markActive) {
            dao.markPresetActive(id)
        }
        if (recordSync) {
            dao.insertSyncRecord(
                SyncQueueEntity(
                    operationType = "PRESET_CALIBRATION",
                    entitySummary = "${config.presetName} (X:${config.offsetXDp.toInt()}dp, Y:${config.offsetYDp.toInt()}dp)",
                    encryptedSnapshotBase64 = encrypted,
                    checksumSha256 = checksum.take(16),
                    resolutionProfile = config.resolutionPreset.label,
                    status = "PENDING"
                )
            )
        }
        return id
    }

    suspend fun activatePreset(presetId: Long) {
        dao.markPresetActive(presetId)
    }

    suspend fun deletePreset(presetId: Long) {
        dao.deletePresetById(presetId)
    }

    suspend fun createPlugin(
        name: String,
        packageIdentifier: String,
        triggerType: PluginTriggerType,
        triggerCondition: String,
        targetModule: IslandModuleType,
        headlineTemplate: String,
        detailTemplate: String,
        accentHex: String,
        recordSync: Boolean = true
    ): Long {
        val payloadJson = JSONObject().apply {
            put("headline", headlineTemplate)
            put("detail", detailTemplate)
            put("packageId", packageIdentifier)
        }.toString()
        val encrypted = cryptoManager.encrypt(payloadJson)
        val checksum = cryptoManager.computeSha256(payloadJson)
        val id = dao.insertPlugin(
            PluginRuleEntity(
                name = name,
                packageIdentifier = packageIdentifier,
                triggerType = triggerType.name,
                triggerCondition = triggerCondition,
                targetModule = targetModule.name,
                encryptedActionPayloadBase64 = encrypted,
                accentHex = accentHex,
                isEnabled = true,
                executionCount = 0
            )
        )
        if (recordSync) {
            dao.insertSyncRecord(
                SyncQueueEntity(
                    operationType = "PLUGIN_REGISTER",
                    entitySummary = "$name [$packageIdentifier]",
                    encryptedSnapshotBase64 = encrypted,
                    checksumSha256 = checksum.take(16),
                    resolutionProfile = "Universal SDK",
                    status = "PENDING"
                )
            )
        }
        return id
    }

    suspend fun togglePlugin(plugin: PluginRuleModel, enabled: Boolean) {
        val existing = dao.getAllPluginsOnce().firstOrNull { it.id == plugin.id } ?: return
        dao.updatePlugin(existing.copy(isEnabled = enabled))
    }

    suspend fun recordPluginExecution(pluginId: Long) {
        val existing = dao.getAllPluginsOnce().firstOrNull { it.id == pluginId } ?: return
        dao.updatePlugin(
            existing.copy(
                executionCount = existing.executionCount + 1,
                lastTriggeredEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePlugin(pluginId: Long) {
        dao.deletePluginById(pluginId)
    }

    suspend fun synchronizeOfflineQueue(): Int {
        return dao.markAllPendingSynced(System.currentTimeMillis())
    }

    suspend fun clearSyncedRecords() {
        dao.clearSyncedHistory()
    }

    suspend fun exportEncryptedVaultJson(currentConfig: IslandConfiguration): String {
        val presets = dao.getAllPresetsOnce()
        val plugins = dao.getAllPluginsOnce()
        val root = JSONObject().apply {
            put("schemaVersion", 1)
            put("exportedAt", System.currentTimeMillis())
            put("activeConfig", JSONObject(serializeConfig(currentConfig)))
            val presetsArr = JSONArray()
            presets.forEach { preset ->
                presetsArr.put(
                    JSONObject().apply {
                        put("name", preset.name)
                        put("encryptedPayload", preset.encryptedPayloadBase64)
                        put("checksum", preset.checksumSha256)
                        put("resolutionTag", preset.resolutionTag)
                    }
                )
            }
            put("presets", presetsArr)
            put("pluginCount", plugins.size)
        }
        return root.toString(2)
    }

    suspend fun importVaultJson(rawJson: String): IslandConfiguration? {
        return runCatching {
            val root = JSONObject(rawJson)
            val activeObj = root.optJSONObject("activeConfig") ?: root
            val config = deserializeConfig(activeObj.toString())
            savePreset(config, markActive = true, recordSync = true)
            config
        }.getOrNull()
    }

    fun serializeConfig(config: IslandConfiguration): String {
        return JSONObject().apply {
            put("presetName", config.presetName)
            put("offsetXDp", config.offsetXDp.toDouble())
            put("offsetYDp", config.offsetYDp.toDouble())
            put("compactWidthDp", config.compactWidthDp.toDouble())
            put("compactHeightDp", config.compactHeightDp.toDouble())
            put("expandedWidthDp", config.expandedWidthDp.toDouble())
            put("expandedHeightDp", config.expandedHeightDp.toDouble())
            put("cornerRadiusDp", config.cornerRadiusDp.toDouble())
            put("splitDropletGapDp", config.splitDropletGapDp.toDouble())
            put("springStiffness", config.springStiffness.toDouble())
            put("springDamping", config.springDamping.toDouble())
            put("accentColorHex", config.accentColorHex)
            put("capsuleSurfaceHex", config.capsuleSurfaceHex)
            put("borderGlowOpacity", config.borderGlowOpacity.toDouble())
            put("anchorMode", config.anchorMode.name)
            put("resolutionPreset", config.resolutionPreset.name)
            put("customScaleFactor", config.customScaleFactor.toDouble())
            put("showAlignmentGrid", config.showAlignmentGrid)
            put("hapticFeedbackEnabled", config.hapticFeedbackEnabled)
            put("themePreference", config.themePreference.name)
        }.toString()
    }

    fun deserializeConfig(jsonString: String): IslandConfiguration {
        val obj = JSONObject(jsonString)
        return IslandConfiguration(
            presetName = obj.optString("presetName", "Custom Studio Preset"),
            offsetXDp = obj.optDouble("offsetXDp", 0.0).toFloat(),
            offsetYDp = obj.optDouble("offsetYDp", 8.0).toFloat(),
            compactWidthDp = obj.optDouble("compactWidthDp", 186.0).toFloat(),
            compactHeightDp = obj.optDouble("compactHeightDp", 36.0).toFloat(),
            expandedWidthDp = obj.optDouble("expandedWidthDp", 348.0).toFloat(),
            expandedHeightDp = obj.optDouble("expandedHeightDp", 178.0).toFloat(),
            cornerRadiusDp = obj.optDouble("cornerRadiusDp", 42.0).toFloat(),
            splitDropletGapDp = obj.optDouble("splitDropletGapDp", 8.0).toFloat(),
            springStiffness = obj.optDouble("springStiffness", 520.0).toFloat(),
            springDamping = obj.optDouble("springDamping", 0.68).toFloat(),
            accentColorHex = obj.optString("accentColorHex", "#FA2D48"),
            capsuleSurfaceHex = obj.optString("capsuleSurfaceHex", "#000000"),
            borderGlowOpacity = obj.optDouble("borderGlowOpacity", 0.28).toFloat(),
            anchorMode = runCatching { NotchAnchorMode.valueOf(obj.optString("anchorMode")) }
                .getOrDefault(NotchAnchorMode.CENTER),
            resolutionPreset = runCatching { ResolutionScalePreset.valueOf(obj.optString("resolutionPreset")) }
                .getOrDefault(ResolutionScalePreset.AUTO_ADAPTIVE),
            customScaleFactor = obj.optDouble("customScaleFactor", 1.0).toFloat(),
            showAlignmentGrid = obj.optBoolean("showAlignmentGrid", false),
            hapticFeedbackEnabled = obj.optBoolean("hapticFeedbackEnabled", true),
            themePreference = runCatching { ThemePreference.valueOf(obj.optString("themePreference")) }
                .getOrDefault(ThemePreference.DARK_OBSIDIAN)
        )
    }
}
