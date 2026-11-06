package com.example

import com.example.model.IslandConfiguration
import com.example.model.IslandModuleState
import com.example.model.ResolutionScalePreset
import com.example.security.CryptoManager
import com.example.ui.components.formatMillisecondsClock
import com.example.ui.components.formatSecondsClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun adaptiveResolutionScaling_computesExpectedScaleAcrossScreenSizes() {
        val autoConfig = IslandConfiguration(resolutionPreset = ResolutionScalePreset.AUTO_ADAPTIVE)
        assertEquals(0.92f, autoConfig.effectiveScale(screenWidthDp = 340), 0.001f)
        assertEquals(1.00f, autoConfig.effectiveScale(screenWidthDp = 412), 0.001f)
        assertEquals(1.07f, autoConfig.effectiveScale(screenWidthDp = 680), 0.001f)
        assertEquals(1.15f, autoConfig.effectiveScale(screenWidthDp = 900), 0.001f)

        val customConfig = IslandConfiguration(
            resolutionPreset = ResolutionScalePreset.CUSTOM,
            customScaleFactor = 1.24f
        )
        assertEquals(1.24f, customConfig.effectiveScale(screenWidthDp = 412), 0.001f)
    }

    @Test
    fun sha256Checksum_isDeterministicAndCollisionResistant() {
        val crypto = CryptoManager("unit_test_alias")
        val hashA1 = crypto.computeSha256("preset_x=12_y=8")
        val hashA2 = crypto.computeSha256("preset_x=12_y=8")
        val hashB = crypto.computeSha256("preset_x=13_y=8")

        assertEquals(64, hashA1.length)
        assertEquals(hashA1, hashA2)
        assertNotEquals(hashA1, hashB)
    }

    @Test
    fun liveActivityProgressAndClockFormatting_areAccurate() {
        val media = IslandModuleState.MediaActivity(elapsedSec = 60, durationSec = 240)
        assertEquals(0.25f, media.progressFraction, 0.001f)
        assertEquals("01:00", formatSecondsClock(60))

        val timer = IslandModuleState.TimerActivity(
            remainingMs = 90_000L,
            totalMs = 180_000L,
            isStopwatchMode = false
        )
        assertEquals(0.50f, timer.progressFraction, 0.001f)
        assertEquals("01:30", formatMillisecondsClock(90_000L))
        assertTrue(timer.priority > media.priority)
    }
}
