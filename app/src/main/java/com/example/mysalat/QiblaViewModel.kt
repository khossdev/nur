package com.example.mysalat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysalat.data.City
import com.example.mysalat.data.CityCatalog
import com.example.mysalat.data.CompassHeadingSource
import com.example.mysalat.data.PrayerStorage
import com.example.mysalat.data.QiblaCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class QiblaUiState(
    val cityName: String = CityCatalog.default.displayName,
    val qiblaDegrees: Float = 0f,
    val headingDegrees: Float? = null,
    val needleDegrees: Float = 0f,
    val sensorAvailable: Boolean = false,
    val aligned: Boolean = false,
    val atKaaba: Boolean = false
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = PrayerStorage(application.applicationContext)
    private val heading = MutableStateFlow<Float?>(null)
    private val sensorRunning = MutableStateFlow(false)

    private val compass = CompassHeadingSource(application.applicationContext) { degrees ->
        heading.value = degrees
    }

    private val city: StateFlow<City> = storage.cityIdFlow
        .map { CityCatalog.byId(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CityCatalog.default
        )

    val uiState: StateFlow<QiblaUiState> =
        combine(city, heading, sensorRunning) { selectedCity, currentHeading, running ->
            buildState(selectedCity, currentHeading, running && compass.isHardwareAvailable)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(CityCatalog.default, null, false)
        )

    fun start() {
        val started = compass.start()
        sensorRunning.value = started
        if (!started) heading.value = null
    }

    fun stop() {
        compass.stop()
        sensorRunning.value = false
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    private fun buildState(
        selectedCity: City,
        currentHeading: Float?,
        sensorAvailable: Boolean
    ): QiblaUiState {
        val qibla = QiblaCalculator.azimuthDegrees(selectedCity).toFloat()
        val atKaaba = QiblaCalculator.isAtKaaba(selectedCity)
        val aligned = !atKaaba &&
            sensorAvailable &&
            currentHeading != null &&
            QiblaCalculator.isAligned(currentHeading.toDouble(), qibla.toDouble())
        val needle = when {
            atKaaba -> 0f
            sensorAvailable && currentHeading != null ->
                QiblaCalculator.shortestDelta(currentHeading.toDouble(), qibla.toDouble()).toFloat()
            else -> qibla
        }
        return QiblaUiState(
            cityName = selectedCity.displayName,
            qiblaDegrees = qibla,
            headingDegrees = currentHeading,
            needleDegrees = needle,
            sensorAvailable = sensorAvailable,
            aligned = aligned,
            atKaaba = atKaaba
        )
    }
}
