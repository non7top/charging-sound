// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings

enum class Status { OK, MISSING, UNKNOWN, MANUAL }

enum class Action { NOTIFICATIONS, BATTERY, AUTOSTART, MIUI_BATTERY }

data class CheckItem(val title: Int, val detail: Int, val status: Status, val action: Action?)

object Persistence {
    // Undocumented AppOps code that MIUI uses for its Autostart switch.
    private const val MIUI_OP_AUTOSTART = 10008

    fun genericItems(context: Context): List<CheckItem> {
        val items = mutableListOf<CheckItem>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            items += CheckItem(
                R.string.notif_perm_title, R.string.notif_perm_detail,
                if (granted) Status.OK else Status.MISSING, Action.NOTIFICATIONS
            )
        }
        val exempt = context.getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName)
        items += CheckItem(
            R.string.battery_title, R.string.battery_detail,
            if (exempt) Status.OK else Status.MISSING, Action.BATTERY
        )
        return items
    }

    fun xiaomiItems(context: Context): List<CheckItem> = listOf(
        CheckItem(R.string.autostart_title, R.string.autostart_detail, miuiAutostart(context), Action.AUTOSTART),
        CheckItem(R.string.miui_battery_title, R.string.miui_battery_detail, Status.MANUAL, Action.MIUI_BATTERY),
        CheckItem(R.string.recents_title, R.string.recents_detail, Status.MANUAL, null),
    )

    fun isXiaomi(): Boolean = isXiaomiFamily(Build.MANUFACTURER, Build.BRAND)

    // A failed read must never be reported as granted.
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun miuiAutostart(context: Context): Status = try {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val check = AppOpsManager::class.java.getMethod(
            "checkOpNoThrow", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java
        )
        val mode = check.invoke(ops, MIUI_OP_AUTOSTART, Process.myUid(), context.packageName) as Int
        if (mode == AppOpsManager.MODE_ALLOWED) Status.OK else Status.MISSING
    } catch (e: Exception) {
        Status.UNKNOWN
    }

    /** Candidate screens for [action], most specific first. */
    fun intentsFor(context: Context, action: Action): List<Intent> {
        val pkg = context.packageName
        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null))
        return when (action) {
            Action.NOTIFICATIONS -> listOf(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, pkg),
                appDetails
            )
            Action.BATTERY -> listOf(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$pkg")),
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                appDetails
            )
            Action.AUTOSTART -> listOf(
                Intent().setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                ),
                appDetails
            )
            Action.MIUI_BATTERY -> listOf(
                Intent().setClassName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                    .putExtra("package_name", pkg)
                    .putExtra("package_label", context.getString(R.string.app_name)),
                appDetails
            )
        }
    }

    /** Starts the first candidate the system accepts. Returns false if none worked. */
    fun launch(context: Context, intents: List<Intent>): Boolean {
        for (intent in intents) {
            try {
                context.startActivity(intent)
                return true
            } catch (e: ActivityNotFoundException) {
                continue
            } catch (e: SecurityException) {
                continue
            }
        }
        return false
    }
}
