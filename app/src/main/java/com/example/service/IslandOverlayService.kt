package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.R
import com.example.model.IslandDisplayMode
import com.example.ui.components.DynamicIslandCapsule
import com.example.ui.theme.IsleStudioTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * System-wide floating Dynamic Island overlay service.
 * Uses WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY so the Dynamic Island
 * hovers seamlessly above other apps with real-time spring animations and X/Y offsets.
 */
class IslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        private const val CHANNEL_ID = "islestudio_overlay_channel"
        private const val NOTIFICATION_ID = 4101
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var composeOverlayView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startForegroundSafely()

        if (Settings.canDrawOverlays(this)) {
            attachOverlayWindow()
        } else {
            IslandRuntimeBridge.logEvent("Overlay permission required to draw over other apps")
            stopSelf()
        }
    }

    private fun startForegroundSafely() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_service_label),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains the floating Dynamic Island overlay above apps"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_active))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        runCatching {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun attachOverlayWindow() {
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return
        windowManager = wm
        val density = resources.displayMetrics.density
        val initialConfig = IslandRuntimeBridge.configuration.value

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = ((initialConfig.anchorMode.defaultXOffsetDp + initialConfig.offsetXDp) * density).roundToInt()
            y = (initialConfig.offsetYDp * density).roundToInt()
        }
        layoutParams = params

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@IslandOverlayService)
            setViewTreeViewModelStoreOwner(this@IslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@IslandOverlayService)
            setContent {
                val config by IslandRuntimeBridge.configuration.collectAsState()
                val mode by IslandRuntimeBridge.displayMode.collectAsState()
                val primary by IslandRuntimeBridge.primaryActivity.collectAsState()
                val secondary by IslandRuntimeBridge.secondaryDropletActivity.collectAsState()

                IsleStudioTheme(darkTheme = true) {
                    DynamicIslandCapsule(
                        configuration = config.copy(offsetXDp = 0f, offsetYDp = 0f),
                        displayMode = mode,
                        primaryActivity = primary,
                        secondaryActivity = secondary,
                        onTapCapsule = {
                            val next = if (mode == IslandDisplayMode.EXPANDED_CARD) {
                                if (secondary != null) IslandDisplayMode.SPLIT_MULTITASK else IslandDisplayMode.COMPACT_PILL
                            } else {
                                IslandDisplayMode.EXPANDED_CARD
                            }
                            IslandRuntimeBridge.setDisplayMode(next)
                        },
                        onLongPressCapsule = {
                            IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.EXPANDED_CARD)
                        },
                        onSwipeDismiss = {
                            IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.IDLE_CUTOUT)
                        },
                        onSwapDroplet = {
                            IslandRuntimeBridge.swapPrimaryAndSecondary()
                        },
                        onMediaPlayPause = {},
                        onMediaSkip = {},
                        onTimerToggle = {},
                        onTimerReset = {},
                        onQuickReply = {}
                    )
                }
            }
        }

        runCatching {
            wm.addView(view, params)
            composeOverlayView = view
            IslandRuntimeBridge.setOverlayServiceRunning(true)
        }.onFailure {
            IslandRuntimeBridge.setOverlayServiceRunning(false)
        }

        serviceScope.launch {
            IslandRuntimeBridge.configuration.collectLatest { updated ->
                val activeView = composeOverlayView ?: return@collectLatest
                val lp = layoutParams ?: return@collectLatest
                val d = resources.displayMetrics.density
                lp.x = ((updated.anchorMode.defaultXOffsetDp + updated.offsetXDp) * d).roundToInt()
                lp.y = (updated.offsetYDp * d).roundToInt()
                runCatching { wm.updateViewLayout(activeView, lp) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        composeOverlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        composeOverlayView = null
        IslandRuntimeBridge.setOverlayServiceRunning(false)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
