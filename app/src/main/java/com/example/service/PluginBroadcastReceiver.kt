package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject

/**
 * Developer SDK BroadcastReceiver allowing third-party apps, Tasker scripts, or ADB intents
 * (`com.example.islestudio.ACTION_PUSH_ACTIVITY`) to inject live activities into the Dynamic Island.
 */
class PluginBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PUSH_ACTIVITY = "com.example.islestudio.ACTION_PUSH_ACTIVITY"
        const val EXTRA_JSON_PAYLOAD = "payload_json"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION_PUSH_ACTIVITY) return

        val explicitJson = intent.getStringExtra(EXTRA_JSON_PAYLOAD)
        if (!explicitJson.isNullOrBlank()) {
            IslandRuntimeBridge.pushExternalJsonActivity(explicitJson)
            return
        }

        // Support individual intent extras for macro/automation tools
        val synthesizedJson = JSONObject().apply {
            put("module", intent.getStringExtra("module") ?: "notification")
            put("title", intent.getStringExtra("title") ?: "External Intent Event")
            put("subtitle", intent.getStringExtra("subtitle") ?: "Dispatched via PluginBroadcastReceiver")
            put("accentHex", intent.getStringExtra("accentHex") ?: "#0A84FF")
            put("expandImmediately", intent.getBooleanExtra("expandImmediately", false))
        }.toString()

        IslandRuntimeBridge.pushExternalJsonActivity(synthesizedJson)
    }
}
