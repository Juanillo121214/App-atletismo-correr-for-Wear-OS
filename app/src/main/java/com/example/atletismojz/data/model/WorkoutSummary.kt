package com.example.atletismojz.data.model

import com.example.atletismojz.data.util.PolylineUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoutSummary(
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val workoutType: WorkoutType,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val avgPaceSecPerKm: Double,
    val intervalConfig: IntervalConfig? = null,
    val multiBlockConfig: MultiBlockIntervalConfig? = null,
    val totalSetsCompleted: Int = 0,
    val manualSplits: List<ManualSplit> = emptyList(),
    val encodedPolyline: String = ""
) {
    val routePoints: List<LocationPoint>
        get() = PolylineUtils.decode(encodedPolyline)

    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val formattedDistance: String
        get() = String.format(Locale.getDefault(), "%.2f km", distanceMeters / 1000.0)

    val formattedPace: String
        get() {
            if (avgPaceSecPerKm <= 0 || avgPaceSecPerKm.isInfinite() || avgPaceSecPerKm > 3600) return "--:--"
            val min = (avgPaceSecPerKm / 60).toInt()
            val sec = (avgPaceSecPerKm % 60).toInt()
            return String.format(Locale.getDefault(), "%d:%02d /km", min, sec)
        }
}
