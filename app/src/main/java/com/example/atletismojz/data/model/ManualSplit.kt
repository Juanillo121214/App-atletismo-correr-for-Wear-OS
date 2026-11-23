package com.example.atletismojz.data.model

import java.util.Locale

data class ManualSplit(
    val splitIndex: Int,
    val isWorkPhase: Boolean,
    val durationSeconds: Long,
    val distanceMeters: Double
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

    val formattedDistance: String
        get() = if (distanceMeters < 1000) {
            String.format(Locale.getDefault(), "%.0f m", distanceMeters)
        } else {
            String.format(Locale.getDefault(), "%.2f km", distanceMeters / 1000.0)
        }

    val avgPaceSecPerKm: Double
        get() = if (distanceMeters > 0) (durationSeconds / (distanceMeters / 1000.0)) else 0.0

    val formattedPace: String
        get() {
            if (avgPaceSecPerKm <= 0 || avgPaceSecPerKm.isInfinite() || avgPaceSecPerKm.isNaN() || avgPaceSecPerKm > 3600) return "--:--"
            val min = (avgPaceSecPerKm / 60).toInt()
            val sec = (avgPaceSecPerKm % 60).toInt()
            return String.format(Locale.getDefault(), "%d:%02d", min, sec)
        }
}
