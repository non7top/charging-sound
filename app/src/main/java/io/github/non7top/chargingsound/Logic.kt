// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import kotlin.random.Random

private val XIAOMI_NAMES = listOf("xiaomi", "redmi", "poco")

fun isXiaomiFamily(manufacturer: String?, brand: String?): Boolean =
    listOf(manufacturer, brand).any { value ->
        value != null && XIAOMI_NAMES.any { it.equals(value, ignoreCase = true) }
    }

/** Accepts an event unless another one was accepted less than [windowMs] earlier. */
class Debouncer(private val windowMs: Long) {
    private var lastAccepted: Long? = null

    fun accept(nowMs: Long): Boolean {
        val last = lastAccepted
        if (last != null && nowMs - last < windowMs) return false
        lastAccepted = nowMs
        return true
    }
}

sealed class SoundSource {
    object Default : SoundSource()
    data class Custom(val uri: String) : SoundSource()
}

fun activeSounds(saved: List<String>, disabled: Set<String>): List<String> = saved.filter { it !in disabled }

/** Picks a random saved sound that can still be opened; the default sound when none can. */
fun pickSource(saved: List<String>, random: Random = Random.Default, canOpen: (String) -> Boolean): SoundSource {
    val uri = saved.filter { it.isNotBlank() }.shuffled(random).firstOrNull(canOpen)
    return if (uri != null) SoundSource.Custom(uri) else SoundSource.Default
}
