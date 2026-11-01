// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("charging_sound", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(value) = sp.edit().putBoolean("enabled", value).apply()

    var soundUris: List<String>
        get() = (sp.getString("sound_uris", null) ?: sp.getString("sound_uri", null) ?: "")
            .split('\n').filter { it.isNotBlank() }
        set(value) = sp.edit().putString("sound_uris", value.joinToString("\n")).remove("sound_uri").apply()

    var notificationPermissionAsked: Boolean
        get() = sp.getBoolean("notif_asked", false)
        set(value) = sp.edit().putBoolean("notif_asked", value).apply()
}
