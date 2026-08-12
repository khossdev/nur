package com.example.mysalat

import com.example.mysalat.data.CityCatalog
import com.example.mysalat.data.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class QiblaCalculatorTest {

    @Test
    fun dakarAndParisHaveDifferentAzimuth() {
        val dakar = QiblaCalculator.azimuthDegrees(CityCatalog.byId("dakar"))
        val paris = QiblaCalculator.azimuthDegrees(CityCatalog.byId("paris"))
        assertNotEquals(dakar, paris)
        assertTrue(dakar in 0.0..360.0)
        assertTrue(paris in 0.0..360.0)
    }

    @Test
    fun shortestDeltaWrapsAcrossNorth() {
        assertEquals(20.0, QiblaCalculator.shortestDelta(350.0, 10.0), 0.001)
        assertEquals(-20.0, QiblaCalculator.shortestDelta(10.0, 350.0), 0.001)
    }

    @Test
    fun makkahIsTreatedAsAtKaaba() {
        assertTrue(QiblaCalculator.isAtKaaba(CityCatalog.byId("makkah")))
        assertFalse(QiblaCalculator.isAtKaaba(CityCatalog.default))
    }

    @Test
    fun alignedWhenWithinEightDegrees() {
        assertTrue(QiblaCalculator.isAligned(0.0, 7.9))
        assertFalse(QiblaCalculator.isAligned(0.0, 8.0))
        assertTrue(QiblaCalculator.isAligned(358.0, 2.0))
        assertTrue(abs(QiblaCalculator.shortestDelta(358.0, 2.0)) < 8.0)
    }
}
