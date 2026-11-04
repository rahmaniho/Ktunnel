package ir.karensoft.karentunnel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ir.karensoft.karentunnel.MainActivity
import ir.karensoft.karentunnel.R
import ir.karensoft.karentunnel.core.connection.ConnectionState

/**
 * Keeps the process alive while a tunnel exists and owns the mandatory
 * persistent notification.
 *
 * Why this exists (documented as F2 in docs/TECHNICAL-FEASIBILITY.md): the
 * WireGuard tunnel library starts its own `VpnService` but never calls
 * `startForeground`. On Android 12+ a cached process with a live tunnel is a
 * process the platform may freeze, which produces a tunnel that "looks" up
 * while traffic stops. Our own foreground service prevents that.
 *
 * This service deliberately does NOT own the tunnel. It only reports state. The
 * connection logic lives in ConnectionManager; keeping the two separate means a
 * notification bug can never take down the tunnel.
 */
class TunnelForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE -> updateNotification(
                state = intent.getStringExtra(EXTRA_STATE) ?: STATE_CONNECTING,
            )
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> startInForeground(NotificationState.Connecting)
        }
        // START_NOT_STICKY: if the OS kills us we do not want a zombie service
        // without a tunnel. Restore logic decides whether to reconnect.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startInForeground(state: NotificationState) {
        val notification = buildNotification(state)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires an explicit type at start time.
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(state: String) {
        val parsed = runCatching { NotificationState.valueOf(state) }.getOrDefault(NotificationState.Connecting)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(parsed))
    }

    private fun buildNotification(state: NotificationState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val (title, text) = when (state) {
            NotificationState.Connected ->
                getString(R.string.notif_title_connected) to getString(R.string.notif_text_connected)
            NotificationState.Degraded ->
                getString(R.string.notif_title_degraded) to getString(R.string.notif_text_degraded)
            NotificationState.Connecting ->
                getString(R.string.notif_title_connecting) to getString(R.string.notif_text_connecting)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_vpn_ic)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_name),
            // LOW: the tunnel status is ambient information, not an alert.
            // A VPN that buzzes on every reconnect trains people to ignore it.
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notif_channel_desc)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    enum class NotificationState { Connecting, Connected, Degraded }

    companion object {
        private const val TAG = "TunnelFgs"
        private const val CHANNEL_ID = "tunnel_status"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_UPDATE = "ir.karensoft.karentunnel.action.UPDATE"
        private const val ACTION_STOP = "ir.karensoft.karentunnel.action.STOP"
        private const val EXTRA_STATE = "state"
        private const val STATE_CONNECTING = "Connecting"

        /** Start (or refresh) the foreground service. Safe to call repeatedly. */
        fun start(context: Context) {
            val intent = Intent(context, TunnelForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, state: NotificationState) {
            val intent = Intent(context, TunnelForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_STATE, state.name)
            }
            runCatching { context.startService(intent) }
                .onFailure { Log.w(TAG, "could not update notification") }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TunnelForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
        }

        /** Maps a connection state to what the notification should say. */
        fun notificationStateFor(state: ConnectionState): NotificationState? = when (state) {
            is ConnectionState.Connected -> NotificationState.Connected
            is ConnectionState.Degraded -> NotificationState.Degraded
            ConnectionState.Preparing,
            ConnectionState.Connecting,
            is ConnectionState.Reconnecting,
            -> NotificationState.Connecting
            else -> null
        }
    }
}
