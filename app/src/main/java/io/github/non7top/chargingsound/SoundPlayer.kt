// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

class SoundPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play() {
        stop()
        val source = pickSource(Prefs(context).soundUris) { canOpen(Uri.parse(it)) }
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        when (source) {
            is SoundSource.Custom -> if (!start(Uri.parse(source.uri))) start(defaultUri)
            SoundSource.Default -> start(defaultUri)
        }
    }

    fun stop() {
        player?.let { release(it) }
    }

    private fun start(uri: Uri): Boolean {
        val mp = MediaPlayer()
        return try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setDataSource(context, uri)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener { release(it) }
            mp.setOnErrorListener { failed, _, _ ->
                release(failed)
                true
            }
            player = mp
            mp.prepareAsync()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Cannot play $uri", e)
            mp.release()
            false
        }
    }

    private fun release(mp: MediaPlayer) {
        if (player === mp) player = null
        mp.release()
    }

    private fun canOpen(uri: Uri): Boolean = try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
    } catch (e: Exception) {
        false
    }

    private companion object {
        const val TAG = "ChargingSound"
    }
}
