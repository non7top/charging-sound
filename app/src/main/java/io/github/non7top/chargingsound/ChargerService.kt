// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log

class ChargerService : Service() {
    private val debouncer = Debouncer(DEBOUNCE_MS)
    private lateinit var player: SoundPlayer

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_CONNECTED &&
                debouncer.accept(SystemClock.elapsedRealtime())
            ) {
                player.play()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = SoundPlayer(this)
        val filter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        enterForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        player.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun enterForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.notif_channel), NotificationManager.IMPORTANCE_LOW)
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(open)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "ChargingSound"
        private const val CHANNEL_ID = "service"
        private const val NOTIFICATION_ID = 1
        private const val DEBOUNCE_MS = 2000L

        fun start(context: Context) {
            try {
                context.startForegroundService(Intent(context, ChargerService::class.java))
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Foreground service start not allowed right now", e)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ChargerService::class.java))
        }
    }
}
