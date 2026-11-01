// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var prefs: Prefs
    private lateinit var enableSwitch: Switch
    private lateinit var soundList: LinearLayout
    private lateinit var player: SoundPlayer
    private lateinit var checklist: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        player = SoundPlayer(this)
        setContentView(buildUi())
    }

    override fun onPause() {
        super.onPause()
        player.stop()
    }

    override fun onResume() {
        super.onResume()
        if (prefs.enabled) ChargerService.start(this)
        refresh()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_SOUND || resultCode != RESULT_OK || data == null) return
        val picked = data.clipData?.let { clip -> (0 until clip.itemCount).map { clip.getItemAt(it).uri } }
            ?: listOfNotNull(data.data)
        val saved = prefs.soundUris.toMutableList()
        for (uri in picked) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                continue
            }
            if (uri.toString() !in saved) saved.add(uri.toString())
        }
        prefs.soundUris = saved
        refresh()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        enableSwitch = Switch(this).apply {
            setText(R.string.enable_label)
            textSize = 18f
        }
        root.addView(enableSwitch)

        root.addView(heading(R.string.sound_heading))
        soundList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(soundList)
        val soundButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        soundButtons.addView(button(R.string.choose_sound) { pickSound() })
        soundButtons.addView(button(R.string.use_default) { useDefaultSound() })
        soundButtons.addView(button(R.string.test_sound) { player.play() })
        root.addView(soundButtons)

        root.addView(heading(R.string.setup_heading))
        checklist = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(checklist)

        return ScrollView(this).apply {
            fitsSystemWindows = true
            addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun refresh() {
        enableSwitch.setOnCheckedChangeListener(null)
        enableSwitch.isChecked = prefs.enabled
        enableSwitch.setOnCheckedChangeListener { _, checked -> setEnabled(checked) }

        soundList.removeAllViews()
        val sounds = prefs.soundUris
        if (sounds.isEmpty()) {
            soundList.addView(TextView(this).apply { setText(R.string.sound_default) })
        } else {
            sounds.forEach { soundList.addView(soundRow(it)) }
            soundList.addView(TextView(this).apply { setText(R.string.sound_random_hint) })
        }

        checklist.removeAllViews()
        Persistence.genericItems(this).forEach { checklist.addView(row(it)) }
        if (Persistence.isXiaomi()) {
            checklist.addView(heading(R.string.xiaomi_heading))
            Persistence.xiaomiItems(this).forEach { checklist.addView(row(it)) }
        }
    }

    private fun setEnabled(enabled: Boolean) {
        prefs.enabled = enabled
        if (enabled) ChargerService.start(this) else ChargerService.stop(this)
    }

    private fun pickSound() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("audio/*")
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_PICK_SOUND)
    }

    private fun useDefaultSound() {
        prefs.soundUris.forEach { releaseGrant(it) }
        prefs.soundUris = emptyList()
        prefs.disabledSounds = emptySet()
        refresh()
    }

    private fun removeSound(uri: String) {
        releaseGrant(uri)
        prefs.soundUris = prefs.soundUris - uri
        prefs.disabledSounds = prefs.disabledSounds - uri
        refresh()
    }

    private fun releaseGrant(uri: String) {
        try {
            contentResolver.releasePersistableUriPermission(Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Grant was already gone.
        }
    }

    private fun soundRow(uri: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val check = CheckBox(this).apply {
            text = displayName(Uri.parse(uri))
            isChecked = uri !in prefs.disabledSounds
            setOnCheckedChangeListener { _, checked ->
                prefs.disabledSounds = if (checked) prefs.disabledSounds - uri else prefs.disabledSounds + uri
            }
        }
        row.addView(check, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(button(R.string.play_sound) { player.preview(uri) })
        row.addView(button(R.string.remove_sound) { removeSound(uri) })
        return row
    }

    private fun displayName(uri: Uri): String = try {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (e: Exception) {
        null
    } ?: uri.lastPathSegment ?: uri.toString()

    private fun onAction(action: Action) {
        if (action == Action.NOTIFICATIONS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !prefs.notificationPermissionAsked
        ) {
            prefs.notificationPermissionAsked = true
            @Suppress("DEPRECATION")
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            return
        }
        Persistence.launch(this, Persistence.intentsFor(this, action))
    }

    private fun row(item: CheckItem): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val statusText = when (item.status) {
            Status.OK -> R.string.status_ok
            Status.MISSING -> R.string.status_missing
            Status.UNKNOWN -> R.string.status_unknown
            Status.MANUAL -> R.string.status_manual
        }
        box.addView(TextView(this).apply {
            text = "${getString(item.title)}: ${getString(statusText)}"
            textSize = 16f
            if (item.status == Status.OK) setTextColor(okColor())
        })
        box.addView(TextView(this).apply { setText(item.detail) })
        item.action?.let { action ->
            box.addView(button(R.string.open) { onAction(action) }.apply {
                (layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            })
        }
        return box
    }

    private fun okColor(): Int {
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        return if (night) 0xFF66BB6A.toInt() else 0xFF2E7D32.toInt()
    }

    private fun heading(text: Int) = TextView(this).apply {
        setText(text)
        textSize = 20f
        setPadding(0, dp(24), 0, dp(8))
    }

    private fun button(text: Int, onClick: () -> Unit) = Button(this).apply {
        setText(text)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val REQUEST_PICK_SOUND = 1
        const val REQUEST_NOTIFICATIONS = 2
    }
}
