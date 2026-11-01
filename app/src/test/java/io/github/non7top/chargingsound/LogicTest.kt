// SPDX-License-Identifier: Apache-2.0
package io.github.non7top.chargingsound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun resolveSourceUsesDefaultWhenNothingSaved() {
        assertEquals(SoundSource.Default, resolveSource(null) { true })
        assertEquals(SoundSource.Default, resolveSource("  ") { true })
    }

    @Test
    fun resolveSourceFallsBackWhenFileCannotBeOpened() {
        assertEquals(SoundSource.Default, resolveSource("content://x/1") { false })
    }

    @Test
    fun resolveSourceUsesSavedUriWhenOpenable() {
        assertEquals(SoundSource.Custom("content://x/1"), resolveSource("content://x/1") { true })
    }
}
