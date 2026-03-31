package com.example.outofroutebuddy.services

import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shows a floating "trip ended" bubble when the detector fires. Tap opens app and "End trip?" dialog.
 * Long-press or drag to bottom zone dismisses without opening app (trip continues).
 * Accessibility: bubble is focusable and has content description; TalkBack announces tap and long-press actions.
 */
@AndroidEntryPoint
class TripEndedOverlayService : Service() {

    @Inject
    lateinit var detector: TripEndedDetector

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var job: kotlinx.coroutines.Job? = null
    /** True only if we called startForeground (e.g. future fallback path); monitoring path does not use foreground. */
    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                // Channel needed later for fallback notification when overlay permission is missing
                createOverlayNotificationChannel()
                // Do not run as foreground here: TripTrackingService already shows the single
                // "Trip in progress" notification; a second foreground would cause double notification.
                startMonitoring()
            }
            ACTION_DISMISS_BUBBLE -> dismissBubble()
            ACTION_TRIP_ENDED_FROM_APP -> {
                (getSystemService(NotificationManager::class.java))?.cancel(FALLBACK_NOTIFICATION_ID)
                dismissBubble()
                detector.onTripEnded()
                stopSelf()
            }
            ACTION_USER_CHOSE_NO -> {
                handleUserChoseNoContinue()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        if (job != null) return
        job = serviceScope.launch {
            launch {
                detector.showArrivalPrompt.collectLatest {
                    if (maybeOpenForegroundPrompt(PROMPT_SURFACE_FOREGROUND)) {
                        notifyTrackingServiceTripEnding()
                        logPromptShown(PROMPT_SURFACE_FOREGROUND)
                    }
                }
            }
            launch {
                detector.showBubble.collectLatest {
                    notifyTrackingServiceTripEnding()
                    if (maybeOpenForegroundPrompt(PROMPT_SURFACE_FOREGROUND)) {
                        logPromptShown(PROMPT_SURFACE_FOREGROUND)
                    } else if (Settings.canDrawOverlays(this@TripEndedOverlayService)) {
                        logPromptShown(PROMPT_SURFACE_OVERLAY)
                        showBubbleView()
                    } else {
                        Log.w(TAG, "Overlay permission not granted; cannot show bubble")
                        logPromptShown(PROMPT_SURFACE_FALLBACK)
                        showFallbackNotification()
                    }
                }
            }
        }
    }

    private fun showFallbackNotification() {
        val now = System.currentTimeMillis()
        val lastPostedAt = prefs.getLong(KEY_LAST_FALLBACK_POSTED_AT, 0L)
        if (!shouldPostFallbackNotification(now, lastPostedAt, FALLBACK_MIN_INTERVAL_MS)) {
            Log.d(TAG, "Skipping fallback notification (deduped)")
            return
        }
        prefs.edit().putLong(KEY_LAST_FALLBACK_POSTED_AT, now).apply()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
            putExtra(EXTRA_TRIP_END_PROMPT_SOURCE, PROMPT_SURFACE_FALLBACK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            FALLBACK_NOTIFICATION_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.trip_ended_fallback_notification_title))
            .setContentText(getString(R.string.trip_ended_fallback_notification_body))
            .setSmallIcon(R.drawable.ic_notification_truck)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        (getSystemService(NotificationManager::class.java))?.notify(FALLBACK_NOTIFICATION_ID, notification)
    }

    private fun showBubbleView() {
        if (bubbleView != null) return
        val wm = windowManager ?: return
        val bubbleSizePx = dp(BUBBLE_SIZE_DP)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(120)
        }
        val container = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.overlay_bubble_background)
            alpha = 0.95f
        }
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.ic_notification_truck)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = getString(R.string.trip_ended_bubble_content_description)
            val paddingPx = dp(24)
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        container.addView(imageView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))
        val handler = Handler(Looper.getMainLooper())
        var longPressFired = false
        val longPressRunnable = Runnable {
            longPressFired = true
            handleUserChoseNoContinue()
        }
        val displayHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.height
        }
        val dismissZoneThresholdY = (displayHeight * 0.9f).toInt()
        val tapSlopPx = dp(8)
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressFired = false
                    handler.postDelayed(longPressRunnable, LONG_PRESS_MS)
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    wm.updateViewLayout(container, params)
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)
                    if (longPressFired) return@setOnTouchListener true
                    val dx = kotlin.math.abs(event.rawX - initialTouchX)
                    val dy = kotlin.math.abs(event.rawY - initialTouchY)
                    val inDismissZone = params.y >= dismissZoneThresholdY - bubbleSizePx
                    if (inDismissZone) {
                        handleUserChoseNoContinue()
                    } else if (dx < tapSlopPx && dy < tapSlopPx) {
                        openAppWithTripEndedDialog(PROMPT_SURFACE_OVERLAY)
                        dismissBubble()
                    }
                }
            }
            false
        }
        bubbleView = container
        try {
            wm.addView(container, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay", e)
            bubbleView = null
            showFallbackNotification()
        }
    }

    private fun openAppWithTripEndedDialog(promptSurface: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
            putExtra(EXTRA_TRIP_END_PROMPT_SOURCE, promptSurface)
        }
        startActivity(intent)
    }

    private fun maybeOpenForegroundPrompt(promptSurface: String): Boolean {
        if (!isAppInForeground()) return false
        val now = System.currentTimeMillis()
        val lastPromptAt = prefs.getLong(KEY_LAST_PROMPT_OPENED_AT, 0L)
        if (now - lastPromptAt < FOREGROUND_PROMPT_MIN_INTERVAL_MS) {
            return false
        }
        prefs.edit().putLong(KEY_LAST_PROMPT_OPENED_AT, now).apply()
        openAppWithTripEndedDialog(promptSurface)
        return true
    }

    private fun isAppInForeground(): Boolean {
        return MainActivity.isVisibleInForeground
    }

    private fun logPromptShown(surface: String) {
        val feedback = detector.getLatestFeedbackSummary()
        (application as? OutOfRouteApplication)?.logAnalyticsEvent(
            "trip_end_prompt_shown",
            mapOf(
                "surface" to surface,
                "confidence_bucket" to feedback.confidenceBucket,
                "completion_bucket" to feedback.completionBucket,
                "speed_bucket" to feedback.speedBucket,
                "motion_bucket" to feedback.rollingDistanceBucket,
                "dwell_bucket" to feedback.dwellBucket,
                "activity_bucket" to feedback.activityBucket,
                "near_origin" to feedback.nearOrigin.toString(),
                "direct_speed" to feedback.hasDirectSpeed.toString(),
            ),
        )
    }

    private fun notifyTrackingServiceTripEnding() {
        try {
            val intent = Intent(this, TripTrackingService::class.java).apply {
                action = ACTION_TRIP_ENDING_DETECTED
            }
            startService(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not notify TripTrackingService of trip ending", e)
        }
    }

    private fun dismissBubble() {
        bubbleView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay", e)
            }
            bubbleView = null
        }
    }

    private fun handleUserChoseNoContinue() {
        detector.onUserChoseNoContinueTrip()
        TripTrackingService.notifyUserContinuedTrip(this)
        dismissBubble()
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        dismissBubble()
        serviceScope.cancel()
        if (isInForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            isInForeground = false
        }
        super.onDestroy()
    }

    private fun createOverlayNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.trip_ended_bubble_content_description),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildMonitoringNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.trip_notification_in_progress))
        .setSmallIcon(R.drawable.ic_notification_truck)
        .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        .setNumber(0)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val CHANNEL_ID = "trip_ended_overlay_v2"
        private const val NOTIFICATION_ID = 9001
        private const val FALLBACK_NOTIFICATION_ID = 9002
        private const val FALLBACK_NOTIFICATION_REQUEST_CODE = 9003
        private const val TAG = "TripEndedOverlay"
        private const val PREFS_NAME = "trip_ended_overlay"
        private const val KEY_LAST_FALLBACK_POSTED_AT = "last_fallback_posted_at"
        private const val KEY_LAST_PROMPT_OPENED_AT = "last_prompt_opened_at"
        private const val FALLBACK_MIN_INTERVAL_MS = 60_000L
        private const val FOREGROUND_PROMPT_MIN_INTERVAL_MS = 60_000L
        private const val BUBBLE_SIZE_DP = 120
        private const val LONG_PRESS_MS = 500L
        const val PROMPT_SURFACE_OVERLAY = "overlay"
        const val PROMPT_SURFACE_FALLBACK = "fallback"
        const val PROMPT_SURFACE_FOREGROUND = "foreground"

        const val ACTION_START_MONITORING = "com.example.outofroutebuddy.TripEndedOverlay.START_MONITORING"
        const val ACTION_DISMISS_BUBBLE = "com.example.outofroutebuddy.TripEndedOverlay.DISMISS"
        const val ACTION_TRIP_ENDED_FROM_APP = "com.example.outofroutebuddy.TripEndedOverlay.TRIP_ENDED_FROM_APP"
        const val ACTION_USER_CHOSE_NO = "com.example.outofroutebuddy.TripEndedOverlay.USER_CHOSE_NO"
        const val EXTRA_OPEN_TRIP_ENDED_DIALOG = "open_trip_ended_dialog"
        const val EXTRA_TRIP_END_PROMPT_SOURCE = "trip_end_prompt_source"

        /**
         * Start overlay monitoring (bubble when trip ended). Uses startService() so we do not
         * show a second foreground notification; TripTrackingService already shows the single
         * "Trip in progress" notification. Avoids double notification in the pull-down.
         */
        fun startWhenTripActive(context: android.content.Context) {
            val intent = Intent(context, TripEndedOverlayService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            context.startService(intent)
        }

        /**
         * Cancel overlay/fallback notifications so they don't linger when a new trip starts.
         * Call from TripTrackingService when starting a new trip so the pull-down shows only "Trip in progress".
         */
        fun cancelStaleNotifications(context: android.content.Context) {
            val nm = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(NOTIFICATION_ID)
            nm?.cancel(FALLBACK_NOTIFICATION_ID)
        }

        fun dismissBubble(context: android.content.Context) {
            val intent = Intent(context, TripEndedOverlayService::class.java).apply {
                action = ACTION_DISMISS_BUBBLE
            }
            context.startService(intent)
        }

        fun notifyTripEndedFromInApp(context: android.content.Context) {
            val intent = Intent(context, TripEndedOverlayService::class.java).apply {
                action = ACTION_TRIP_ENDED_FROM_APP
            }
            context.startService(intent)
        }

        fun notifyUserChoseNoContinue(context: android.content.Context) {
            val intent = Intent(context, TripEndedOverlayService::class.java).apply {
                action = ACTION_USER_CHOSE_NO
            }
            context.startService(intent)
        }

        internal fun shouldPostFallbackNotification(
            nowMillis: Long,
            lastPostedAtMillis: Long,
            minIntervalMillis: Long,
        ): Boolean = nowMillis - lastPostedAtMillis >= minIntervalMillis
    }
}
