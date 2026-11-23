package com.example.atletismojz.data.model

data class IntervalConfig(
    val workSeconds: Int = 30,     // Tiempo corriendo (ej. 30 seg)
    val restSeconds: Int = 30,     // Tiempo trotando/recuperando (ej. 30 seg o 20 seg)
    val totalSets: Int = 10        // Número de series (ej. 10)
)
