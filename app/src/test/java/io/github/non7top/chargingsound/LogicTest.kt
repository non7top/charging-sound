// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun xiaomiFamilyMatchesManufacturerOrBrandCaseInsensitively() {
        assertTrue(isXiaomiFamily("Xiaomi", "Redmi"))
        assertTrue(isXiaomiFamily("xiaomi", null))
        assertTrue(isXiaomiFamily("unknown", "POCO"))
        assertTrue(isXiaomiFamily(null, "redmi"))
    }

    @Test
    fun xiaomiFamilyRejectsOthers() {
        assertFalse(isXiaomiFamily("samsung", "samsung"))
        assertFalse(isXiaomiFamily(null, null))
        assertFalse(isXiaomiFamily("Xiaomi Inc", "other"))
    }

    @Test
    fun debouncerRejectsEventsInsideWindow() {
        val d = Debouncer(2000)
        assertTrue(d.accept(10_000))
        assertFalse(d.accept(11_999))
        assertTrue(d.accept(12_000))
    }

    @Test
    fun debouncerWindowStartsAtLastAcceptedEvent() {
        val d = Debouncer(2000)
        assertTrue(d.accept(0))
        assertFalse(d.accept(1500))
        assertTrue(d.accept(2000))
    }

    @Test
    fun activeSoundsDropsUncheckedOnesAndKeepsOrder() {
        val saved = listOf("a", "b", "c")
        assertEquals(listOf("a", "c"), activeSounds(saved, setOf("b", "gone")))
        assertEquals(emptyList<String>(), activeSounds(saved, saved.toSet()))
    }

    @Test
    fun pickSourceUsesDefaultWhenNothingSaved() {
        assertEquals(SoundSource.Default, pickSource(emptyList()) { true })
        assertEquals(SoundSource.Default, pickSource(listOf("  ")) { true })
    }

    @Test
    fun pickSourceFallsBackWhenNoFileCanBeOpened() {
        assertEquals(SoundSource.Default, pickSource(listOf("content://x/1", "content://x/2")) { false })
    }

    @Test
    fun pickSourceUsesSingleOpenableUri() {
        assertEquals(SoundSource.Custom("content://x/1"), pickSource(listOf("content://x/1")) { true })
    }

    @Test
    fun pickSourceSkipsUnopenableUris() {
        val saved = listOf("content://x/1", "content://x/2", "content://x/3")
        repeat(50) {
            assertEquals(SoundSource.Custom("content://x/2"), pickSource(saved) { it == "content://x/2" })
        }
    }

    @Test
    fun pickSourceChoosesAmongAllOpenableUris() {
        val saved = listOf("content://x/1", "content://x/2", "content://x/3")
        val random = Random(42)
        val seen = (1..200).map { (pickSource(saved, random) { true } as SoundSource.Custom).uri }.toSet()
        assertEquals(saved.toSet(), seen)
    }
}
