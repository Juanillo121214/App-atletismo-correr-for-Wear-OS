package com.example.atletismojz.data.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestampMs: Long = System.currentTimeMillis()
)
