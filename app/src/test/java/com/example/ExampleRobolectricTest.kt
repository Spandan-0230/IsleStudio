package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.IslandDatabase
import com.example.data.IslandRepository
import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.IslandModuleType
import com.example.model.NotchAnchorMode
import com.example.model.PluginTriggerType
import com.example.security.CryptoManager
import com.example.service.IslandRuntimeBridge
import com.example.service.PluginBroadcastReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameResource_matchesIsleStudio() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("IsleStudio", context.getString(R.string.app_name))
    }

    @Test
    fun cryptoManager_encryptsAndDecryptsWithAes256Gcm() {
        val crypto = CryptoManager("robolectric_vault_key")
        val secretJson = """{"presetName":"Pro Studio","offsetXDp":-42.0,"offsetYDp":14.0}"""
        val encrypted = crypto.encrypt(secretJson)

        assertNotEquals(secretJson, encrypted)
        val decrypted = crypto.decrypt(encrypted)
        assertEquals(secretJson, decrypted)
    }

    @Test
    fun repository_persistsEncryptedPresetsPluginsAndOfflineSyncQueue() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, IslandDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = IslandRepository(db.islandDao(), CryptoManager("test_repo_key"))

        repository.ensureSeeded()
        val initialPresets = repository.presetsFlow.first()
        assertTrue(initialPresets.size >= 3)

        val customConfig = IslandConfiguration(
            presetName = "Left Cutout Calibrated",
            offsetXDp = -88f,
            offsetYDp = 12f,
            springStiffness = 740f,
            springDamping = 0.58f,
            anchorMode = NotchAnchorMode.LEFT_PUNCH
        )
        repository.savePreset(customConfig, markActive = true, recordSync = true)

        val updatedPresets = repository.presetsFlow.first()
        val loaded = updatedPresets.firstOrNull { it.name == "Left Cutout Calibrated" }
        assertNotNull(loaded)
        assertEquals(-88f, loaded!!.config.offsetXDp, 0.01f)
        assertEquals(12f, loaded.config.offsetYDp, 0.01f)

        // Verify offline sync queue recorded pending operation and transitions to SYNCED
        val pendingBefore = repository.syncQueueFlow.first().count { it.status == "PENDING" }
        assertTrue(pendingBefore >= 1)

        val syncedCount = repository.synchronizeOfflineQueue()
        assertTrue(syncedCount >= 1)
        val pendingAfter = repository.syncQueueFlow.first().count { it.status == "PENDING" }
        assertEquals(0, pendingAfter)

        // Verify plugin creation and encrypted template storage
        repository.createPlugin(
            name = "Build Server Alert",
            packageIdentifier = "io.ci.runner",
            triggerType = PluginTriggerType.WEBHOOK_POLL,
            triggerCondition = "status == success",
            targetModule = IslandModuleType.TIMER,
            headlineTemplate = "Build #812 Passed",
            detailTemplate = "Deployed in 42s",
            accentHex = "#34C759"
        )
        val plugins = repository.pluginsFlow.first()
        assertTrue(plugins.any { it.name == "Build Server Alert" && it.headlineTemplate == "Build #812 Passed" })

        // Verify JSON vault export and import round-trip
        val exported = repository.exportEncryptedVaultJson(customConfig)
        val reimported = repository.importVaultJson(exported)
        assertNotNull(reimported)
        assertEquals("Left Cutout Calibrated", reimported!!.presetName)

        db.close()
    }

    @Test
    fun pluginBroadcastReceiver_dispatchesExternalJsonActivityToIslandBridge() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = PluginBroadcastReceiver()
        val intent = Intent(PluginBroadcastReceiver.ACTION_PUSH_ACTIVITY).apply {
            putExtra(
                PluginBroadcastReceiver.EXTRA_JSON_PAYLOAD,
                """
                {
                  "module": "delivery",
                  "title": "Courier Arriving Now",
                  "subtitle": "Order #904 at Lobby",
                  "badge": "1 MIN",
                  "eta": "09:45",
                  "progress": 0.95,
                  "accentHex": "#34C759",
                  "expandImmediately": true
                }
                """.trimIndent()
            )
        }
        receiver.onReceive(context, intent)

        assertEquals(IslandDisplayMode.EXPANDED_CARD, IslandRuntimeBridge.displayMode.value)
        val currentPrimary = IslandRuntimeBridge.primaryActivity.value
        assertTrue(currentPrimary is IslandModuleState.NavigationTrackerActivity)
        assertEquals(
            "Courier Arriving Now",
            (currentPrimary as IslandModuleState.NavigationTrackerActivity).primaryInstruction
        )
    }

    @Test
    fun composeStudioUi_enforcesWelcomePermissionGateAndNavigatesDashboard() {
        // Reset onboarding state for deterministic test run
        IslandRuntimeBridge.updateOnboardingState {
            com.example.model.OnboardingPermissionState()
        }
        composeTestRule.waitForIdle()

        // 1. Verify Welcome Screen is shown with live Dynamic Island preview and Continue button
        composeTestRule.onNodeWithTag("dynamic_island_capsule").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_continue_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 2. Verify Mandatory Permissions Gate Popup appears and blocks Dashboard entry before permissions are granted
        composeTestRule.onNodeWithTag("permissions_gate_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("enter_dashboard_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("mandatory_permission_warning").assertIsDisplayed()

        // 3. Grant all 5 mandatory permissions & background exemptions, then enter Dashboard
        composeTestRule.onNodeWithTag("grant_all_permissions_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("enter_dashboard_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 4. Verify Dashboard is now active and Dynamic Island morphs to Expanded Card
        composeTestRule.onNodeWithTag("mode_chip_expanded_card").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(IslandDisplayMode.EXPANDED_CARD, IslandRuntimeBridge.displayMode.value)

        // 5. Navigate to X/Y & Physics Calibration tab and step X offset
        composeTestRule.onNodeWithTag("nav_tab_calibration").performClick()
        composeTestRule.waitForIdle()
        val initialX = IslandRuntimeBridge.configuration.value.offsetXDp
        composeTestRule.onNodeWithTag("step_x_plus").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialX + 1f, IslandRuntimeBridge.configuration.value.offsetXDp, 0.01f)

        // 6. Navigate to Plugin SDK tab and dispatch live JSON payload
        composeTestRule.onNodeWithTag("nav_tab_plugins").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dispatch_json_broadcast_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 7. Navigate to Vault & System tab
        composeTestRule.onNodeWithTag("nav_tab_vault_sync").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("save_encrypted_preset_button")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
