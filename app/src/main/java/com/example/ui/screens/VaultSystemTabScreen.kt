package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedIslandPreset
import com.example.model.IslandConfiguration
import com.example.model.ResolutionScalePreset
import com.example.model.SyncQueueRecord
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import java.util.Locale

@Composable
fun VaultSystemTabScreen(
    configuration: IslandConfiguration,
    savedPresets: List<SavedIslandPreset>,
    syncQueue: List<SyncQueueRecord>,
    isOverlayRunning: Boolean,
    exportedJson: String?,
    canDrawOverlays: Boolean,
    isNotifListenerGranted: Boolean,
    onToggleOverlayService: (Boolean) -> Unit,
    onUpdateResolutionPreset: (ResolutionScalePreset, Float) -> Unit,
    onSavePreset: (String) -> Unit,
    onLoadPreset: (SavedIslandPreset) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onSyncOfflineQueue: () -> Unit,
    onExportVault: () -> Unit,
    onImportVault: (String) -> Unit,
    onDismissExportModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val screenConfig = LocalConfiguration.current
    var presetNameInput by remember { mutableStateOf(configuration.presetName) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var notifPermissionGranted by remember { mutableStateOf(true) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifPermissionGranted = granted
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. System-Wide Overlay & Seamless Permission Onboarding Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "System-Wide Floating Overlay",
                                style = MaterialTheme.typography.titleMedium,
                                color = AlabasterPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Hover Dynamic Island above all Android apps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZincSecondary
                            )
                        }
                    }
                    Switch(
                        checked = isOverlayRunning,
                        onCheckedChange = { onToggleOverlayService(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF34C759)
                        ),
                        modifier = Modifier.testTag("system_overlay_switch")
                    )
                }

                // Guided Permission Rows
                PermissionStatusRow(
                    title = "Display Over Other Apps (SYSTEM_ALERT_WINDOW)",
                    isGranted = canDrawOverlays,
                    actionLabel = if (canDrawOverlays) "Granted" else "Configure",
                    onClick = {
                        runCatching {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                )

                PermissionStatusRow(
                    title = "Live Notification Bridge (NotificationListener)",
                    isGranted = isNotifListenerGranted,
                    actionLabel = if (isNotifListenerGranted) "Active" else "Enable Access",
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    }
                )

                PermissionStatusRow(
                    title = "Real-Time Activity Alerts (POST_NOTIFICATIONS)",
                    isGranted = notifPermissionGranted,
                    actionLabel = "Verify",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }

        // 2. Adaptive Screen Resolution & Accessibility Scaling Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null,
                            tint = AlabasterPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Resolution & Accessibility Scaling",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${screenConfig.screenWidthDp}x${screenConfig.screenHeightDp}dp",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZincMuted
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        ResolutionScalePreset.AUTO_ADAPTIVE,
                        ResolutionScalePreset.COMPACT_PHONE,
                        ResolutionScalePreset.FOLDABLE_WIDE,
                        ResolutionScalePreset.CUSTOM
                    ).forEach { preset ->
                        val selected = configuration.resolutionPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) AlabasterPrimary else StudioSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onUpdateResolutionPreset(preset, configuration.customScaleFactor)
                                }
                                .testTag("res_preset_${preset.name.lowercase(Locale.US)}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.Black else ZincSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Custom Accessibility Scale Factor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZincSecondary
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.2fx",
                            configuration.effectiveScale(screenConfig.screenWidthDp)
                        ),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelLarge,
                        color = AlabasterPrimary
                    )
                }
                Slider(
                    value = configuration.customScaleFactor,
                    onValueChange = {
                        onUpdateResolutionPreset(ResolutionScalePreset.CUSTOM, it)
                    },
                    valueRange = 0.75f..1.35f,
                    colors = SliderDefaults.colors(
                        thumbColor = AlabasterPrimary,
                        activeTrackColor = AlabasterPrimary,
                        inactiveTrackColor = StudioSurfaceElevated
                    )
                )
            }
        }

        // 3. AES-256-GCM Encrypted Preset Vault & Backup Export/Import
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFF9F0A),
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "AES-256-GCM Encrypted Preset Vault",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hardware Keystore encryption + SHA-256 integrity verification",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZincSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Profile Name", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = AlabasterPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlabasterPrimary,
                            unfocusedBorderColor = HairlineBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_name_input")
                    )
                    Button(
                        onClick = { onSavePreset(presetNameInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlabasterPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("save_encrypted_preset_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportVault,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioSurfaceElevated,
                            contentColor = AlabasterPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_vault_button")
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Vault JSON", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioSurfaceElevated,
                            contentColor = AlabasterPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_vault_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import JSON", style = MaterialTheme.typography.labelSmall)
                    }
                }

                savedPresets.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLoadPreset(preset) }
                            .testTag("saved_preset_row_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AlabasterPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (preset.isActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active Preset",
                                            tint = Color(0xFF34C759),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "X:${preset.config.offsetXDp.toInt()}dp Y:${preset.config.offsetYDp.toInt()}dp • SHA256:${preset.checksumSha256.take(10)}…",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    color = ZincMuted
                                )
                            }
                            IconButton(
                                onClick = { onDeletePreset(preset.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete preset",
                                    tint = ZincMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Offline Synchronization Queue Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Offline Synchronization Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                color = AlabasterPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            val pendingCount = syncQueue.count { it.status == "PENDING" }
                            Text(
                                text = "$pendingCount pending • ${syncQueue.size} total encrypted state ops",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZincSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0A84FF),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSyncOfflineQueue() }
                            .testTag("sync_offline_queue_button")
                    ) {
                        Text(
                            text = "Sync Now",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                syncQueue.take(4).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.operationType}: ${item.entitySummary}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlabasterPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "SHA-256: ${item.checksumSha256} • ${item.resolutionProfile}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                color = ZincMuted
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.status == "SYNCED") {
                                Color(0xFF34C759).copy(alpha = 0.16f)
                            } else {
                                Color(0xFFFF9F0A).copy(alpha = 0.16f)
                            }
                        ) {
                            Text(
                                text = item.status,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                color = if (item.status == "SYNCED") Color(0xFF34C759) else Color(0xFFFF9F0A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (exportedJson != null) {
        AlertDialog(
            onDismissRequest = onDismissExportModal,
            containerColor = StudioSurface,
            title = {
                Text(
                    text = "Encrypted Vault JSON Export",
                    color = AlabasterPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = exportedJson,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = AlabasterPrimary
                    ),
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissExportModal) {
                    Text("Done", color = AlabasterPrimary)
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = StudioSurface,
            title = {
                Text(
                    text = "Import Preset or Vault JSON",
                    color = AlabasterPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    placeholder = { Text("Paste JSON configuration here...", color = ZincMuted) },
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = AlabasterPrimary
                    ),
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onImportVault(importJsonText)
                        showImportDialog = false
                    }
                ) {
                    Text("Import", color = Color(0xFF34C759))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = ZincSecondary)
                }
            }
        )
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = StudioSurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF34C759) else Color(0xFFFF9F0A),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlabasterPrimary
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) Color(0xFF34C759).copy(alpha = 0.18f) else AlabasterPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) Color(0xFF34C759) else Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
