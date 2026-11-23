package com.example.atletismojz.data.model

import java.util.Locale

data class HeartRateZone(
    val name: String,
    val description: String,
    val colorHex: Long
)

data class WorkoutData(
    val type: WorkoutType = WorkoutType.CONTINUOUS_RUN,
    val state: WorkoutState = WorkoutState.IDLE,
    val totalDurationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentHeartRate: Int = 0,
    val avgHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val currentPaceSecPerKm: Double = 0.0,
    val routePoints: List<LocationPoint> = emptyList(),
    val intervalConfig: IntervalConfig = IntervalConfig(),
    val currentSet: Int = 1,
    val isWorkPhase: Boolean = true,
    val phaseTimeRemainingSec: Int = 30,
    val totalLaps: Int = 0,
    val lapDistancesMeters: List<Double> = emptyList(),

    // Objetivos
    val targetTimeSeconds: Long = 0L,
    val targetDistanceMeters: Double = 0.0,
    val lastKmCompleted: Int = 0,
    val lastKmNoticeText: String? = null,
    val userMaxHeartRate: Int = 190,

    // Series Manuales por Botón
    val manualSplits: List<ManualSplit> = emptyList(),
    val currentSplitDurationSec: Long = 0L,
    val currentSplitDistanceMeters: Double = 0.0,

    // Cambios Multi-Bloque
    val multiBlockConfig: MultiBlockIntervalConfig = MultiBlockIntervalConfig(),
    val currentBlockIndex: Int = 0
) {
    val formattedPace: String
        get() {
            if (currentPaceSecPerKm <= 0 || currentPaceSecPerKm.isInfinite() || currentPaceSecPerKm.isNaN() || currentPaceSecPerKm > 3600) {
                return "--:--"
            }
            val min = (currentPaceSecPerKm / 60).toInt()
            val sec = (currentPaceSecPerKm % 60).toInt()
            return String.format(Locale.getDefault(), "%d:%02d", min, sec)
        }

    val formattedDistanceKm: String
        get() = String.format(Locale.getDefault(), "%.2f km", distanceMeters / 1000.0)

    val formattedDuration: String
        get() {
            val hours = totalDurationSeconds / 3600
            val minutes = (totalDurationSeconds % 3600) / 60
            val seconds = totalDurationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSplitDuration: String
        get() {
            val min = currentSplitDurationSec / 60
            val sec = currentSplitDurationSec % 60
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
        }

    val formattedSplitDistance: String
        get() = if (currentSplitDistanceMeters < 1000) {
            String.format(Locale.getDefault(), "%.0f m", currentSplitDistanceMeters)
        } else {
            String.format(Locale.getDefault(), "%.2f km", currentSplitDistanceMeters / 1000.0)
        }

    val heartRateZone: HeartRateZone
        get() {
            if (currentHeartRate <= 0) {
                return HeartRateZone("Z0", "Reposo", 0xFF9E9E9E)
            }
            val pct = (currentHeartRate.toFloat() / userMaxHeartRate.toFloat()) * 100f
            return when {
                pct < 60f -> HeartRateZone("Z1", "Recuperación", 0xFF2196F3)
                pct < 70f -> HeartRateZone("Z2", "Trote / Suave", 0xFF4CAF50)
                pct < 80f -> HeartRateZone("Z3", "Aeróbico", 0xFFFFEB3B)
                pct < 90f -> HeartRateZone("Z4", "Umbral", 0xFFFF9800)
                else -> HeartRateZone("Z5", "Máximo", 0xFFFF5252)
            }
        }
}
