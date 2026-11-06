package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState

/**
 * Bridges real Android status-bar notifications into the Dynamic Island activity multiplexer.
 */
class IslandNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        IslandRuntimeBridge.setNotificationListenerConnected(true)
    }

    override fun onListenerDisconnected() {
        IslandRuntimeBridge.setNotificationListenerConnected(false)
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        if (sbn.packageName == packageName) return // Ignore our own foreground service notification

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }
            ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?: "New notification activity"

        val appLabel = sbn.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        val activity = IslandModuleState.NotificationBatteryActivity(
            appName = appLabel,
            sender = title,
            message = text,
            batteryPercent = 84,
            isCharging = false,
            isBatteryAlertMode = false,
            timestampLabel = "now"
        )
        IslandRuntimeBridge.setPrimaryActivity(activity)
        IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.COMPACT_PILL)
        IslandRuntimeBridge.logEvent("Captured notification from $appLabel: $title")
    }
}
