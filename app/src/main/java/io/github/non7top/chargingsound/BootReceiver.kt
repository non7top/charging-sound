// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if ((action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) &&
            Prefs(context).enabled
        ) {
            ChargerService.start(context)
        }
    }
}
