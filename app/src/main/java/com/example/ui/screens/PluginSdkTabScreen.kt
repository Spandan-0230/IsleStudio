package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IslandModuleType
import com.example.model.PluginRuleModel
import com.example.model.PluginTriggerType
import com.example.model.parseHexColorSafely
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import java.util.Locale

@Composable
fun PluginSdkTabScreen(
    plugins: List<PluginRuleModel>,
    onCreatePlugin: (String, String, PluginTriggerType, String, IslandModuleType, String, String, String) -> Unit,
    onTriggerPlugin: (PluginRuleModel) -> Unit,
    onTogglePlugin: (PluginRuleModel, Boolean) -> Unit,
    onDeletePlugin: (Long) -> Unit,
    onDispatchRawJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pluginName by remember { mutableStateOf("") }
    var packageId by remember { mutableStateOf("io.dev.custom.hook") }
    var selectedTrigger by remember { mutableStateOf(PluginTriggerType.WEBHOOK_POLL) }
    var triggerCondition by remember { mutableStateOf("POST /api/v1/deploy") }
    var selectedTargetModule by remember { mutableStateOf(IslandModuleType.NAVIGATION) }
    var headlineTemplate by remember { mutableStateOf("Production Deploy v2.4") }
    var detailTemplate by remember { mutableStateOf("All 42 health checks passed • 0ms drift") }

    var jsonPayloadInput by remember {
        mutableStateOf(
            """
            {
              "module": "delivery",
              "title": "Drone Courier #08 Arriving",
              "subtitle": "Dropoff at Rooftop Pad B • 90 sec",
              "badge": "90s",
              "eta": "Now",
              "progress": 0.91,
              "accentHex": "#34C759",
              "expandImmediately": true
            }
            """.trimIndent()
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Visual Action & Webhook Plugin Builder
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
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = AlabasterPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Visual Plugin & Webhook Builder",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlabasterPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioCompactTextField(
                        value = pluginName,
                        onValueChange = { pluginName = it },
                        label = "Plugin Name",
                        placeholder = "e.g. Deploy Hook",
                        modifier = Modifier.weight(1f),
                        testTag = "input_plugin_name"
                    )
                    StudioCompactTextField(
                        value = packageId,
                        onValueChange = { packageId = it },
                        label = "Developer Package ID",
                        placeholder = "com.app.plugin",
                        modifier = Modifier.weight(1f),
                        testTag = "input_plugin_package"
                    )
                }

                Text(
                    text = "TRIGGER SOURCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZincMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PluginTriggerType.entries.forEach { trigger ->
                        val selected = selectedTrigger == trigger
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) AlabasterPrimary else StudioSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTrigger = trigger }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = trigger.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.Black else ZincSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "TARGET ISLAND MODULE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZincMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IslandModuleType.entries.forEach { mod ->
                        val selected = selectedTargetModule == mod
                        val accent = parseHexColorSafely(mod.defaultAccentHex)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) accent else StudioSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTargetModule = mod }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mod.id.uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.Black else ZincSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioCompactTextField(
                        value = headlineTemplate,
                        onValueChange = { headlineTemplate = it },
                        label = "Capsule Headline",
                        placeholder = "Primary status text",
                        modifier = Modifier.weight(1f),
                        testTag = "input_plugin_headline"
                    )
                    StudioCompactTextField(
                        value = detailTemplate,
                        onValueChange = { detailTemplate = it },
                        label = "Expanded Telemetry Detail",
                        placeholder = "Secondary detail",
                        modifier = Modifier.weight(1f),
                        testTag = "input_plugin_detail"
                    )
                }

                Button(
                    onClick = {
                        onCreatePlugin(
                            pluginName.ifBlank { "Webhook Live Pill" },
                            packageId,
                            selectedTrigger,
                            triggerCondition,
                            selectedTargetModule,
                            headlineTemplate,
                            detailTemplate,
                            selectedTargetModule.defaultAccentHex
                        )
                        pluginName = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlabasterPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_plugin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypt & Register Plugin Hook",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Developer JSON Broadcast SDK & Live Console
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
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Developer JSON Intent API Console",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Action: com.example.islestudio.ACTION_PUSH_ACTIVITY",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = ZincMuted
                        )
                    }
                }

                OutlinedTextField(
                    value = jsonPayloadInput,
                    onValueChange = { jsonPayloadInput = it },
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = AlabasterPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = HairlineBorder,
                        focusedContainerColor = Color(0xFF0B0B0E),
                        unfocusedContainerColor = Color(0xFF0B0B0E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 5,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("json_sdk_editor")
                )

                Button(
                    onClick = { onDispatchRawJson(jsonPayloadInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0A84FF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dispatch_json_broadcast_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dispatch JSON Payload to Dynamic Island",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Registered Encrypted Plugins List
        Text(
            text = "REGISTERED PLUGIN HOOKS (${plugins.size})",
            style = MaterialTheme.typography.labelLarge,
            color = ZincMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        plugins.forEach { plugin ->
            val accent = parseHexColorSafely(plugin.accentHex)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StudioSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
                    .testTag("plugin_item_${plugin.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = plugin.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = AlabasterPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accent.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = plugin.targetModule.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${plugin.packageIdentifier} • ${plugin.triggerCondition}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = ZincMuted
                        )
                        Text(
                            text = "${plugin.headlineTemplate} — ${plugin.detailTemplate} (Runs: ${plugin.executionCount})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZincSecondary,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTriggerPlugin(plugin) }
                                .testTag("run_plugin_${plugin.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Fire Plugin",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Fire",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Switch(
                            checked = plugin.isEnabled,
                            onCheckedChange = { onTogglePlugin(plugin, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AlabasterPrimary
                            )
                        )
                        IconButton(
                            onClick = { onDeletePlugin(plugin.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete plugin",
                                tint = ZincMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioCompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.labelSmall, color = ZincMuted) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = AlabasterPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AlabasterPrimary,
            unfocusedBorderColor = HairlineBorder,
            focusedLabelColor = AlabasterPrimary,
            unfocusedLabelColor = ZincSecondary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    )
}
