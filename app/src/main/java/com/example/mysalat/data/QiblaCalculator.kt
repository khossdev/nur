package com.example.mysalat.data

import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Qibla
import kotlin.math.abs

/**
 * Offline Qibla azimuth from a [City] using Adhan. Degrees clockwise from true north.
 */
object QiblaCalculator {

    const val ALIGNED_THRESHOLD_DEGREES = 8.0

    fun azimuthDegrees(city: City): Double =
        Qibla(Coordinates(city.latitude, city.longitude)).direction

    fun isAtKaaba(city: City): Boolean = city.id == "makkah"

    /** Shortest signed turn from [from] to [to], in (-180, 180]. */
    fun shortestDelta(from: Double, to: Double): Double {
        val delta = normalize(to - from)
        return if (delta > 180.0) delta - 360.0 else delta
    }

    fun isAligned(headingDegrees: Double, qiblaDegrees: Double): Boolean =
        abs(shortestDelta(headingDegrees, qiblaDegrees)) < ALIGNED_THRESHOLD_DEGREES

    fun normalize(degrees: Double): Double {
        val wrapped = degrees % 360.0
        return if (wrapped < 0.0) wrapped + 360.0 else wrapped
    }
}
