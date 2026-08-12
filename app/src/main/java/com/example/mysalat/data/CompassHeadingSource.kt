package com.example.mysalat.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs

/**
 * Device heading in degrees clockwise from magnetic/true north as reported by
 * the rotation vector (accelerometer + magnetometer fallback). No location.
 */
class CompassHeadingSource(
    private val context: Context,
    private val onHeadingChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var hasGravity = false
    private var hasGeomagnetic = false
    private var usingRotationVector = false
    private var smoothed: Float? = null
    private var listening = false

    val isHardwareAvailable: Boolean
        get() = rotationVector != null || (accelerometer != null && magnetometer != null)

    fun start(): Boolean {
        if (listening) return isHardwareAvailable
        if (rotationVector != null) {
            usingRotationVector = true
            listening = sensorManager.registerListener(
                this,
                rotationVector,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else if (accelerometer != null && magnetometer != null) {
            usingRotationVector = false
            val acc = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            val mag = sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            listening = acc && mag
        }
        return listening
    }

    fun stop() {
        if (!listening) return
        sensorManager.unregisterListener(this)
        listening = false
        hasGravity = false
        hasGeomagnetic = false
        smoothed = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val heading = when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> headingFromRotationVector(event.values)
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
                hasGravity = true
                headingFromAccMag()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                hasGeomagnetic = true
                headingFromAccMag()
            }
            else -> null
        } ?: return

        val previous = smoothed
        val next = if (previous == null) {
            heading
        } else {
            val delta = QiblaCalculator.shortestDelta(previous.toDouble(), heading.toDouble())
            QiblaCalculator.normalize(previous + (delta * SMOOTHING)).toFloat()
        }
        smoothed = next
        if (previous == null || abs(QiblaCalculator.shortestDelta(previous.toDouble(), next.toDouble())) >= MIN_EMIT_DEGREES) {
            onHeadingChanged(next)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun headingFromRotationVector(values: FloatArray): Float? {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        return headingFromMatrix(rotationMatrix)
    }

    private fun headingFromAccMag(): Float? {
        if (!hasGravity || !hasGeomagnetic) return null
        val ok = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (!ok) return null
        return headingFromMatrix(rotationMatrix)
    }

    private fun headingFromMatrix(matrix: FloatArray): Float {
        val (worldX, worldY) = remapAxes()
        SensorManager.remapCoordinateSystem(matrix, worldX, worldY, remappedMatrix)
        SensorManager.getOrientation(remappedMatrix, orientation)
        val azimuth = Math.toDegrees(orientation[0].toDouble())
        return QiblaCalculator.normalize(azimuth).toFloat()
    }

    private fun remapAxes(): Pair<Int, Int> {
        return when (displayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
    }

    private fun displayRotation(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.rotation ?: Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.rotation
        }
    }

    private companion object {
        const val SMOOTHING = 0.18
        const val MIN_EMIT_DEGREES = 0.4
    }
}
