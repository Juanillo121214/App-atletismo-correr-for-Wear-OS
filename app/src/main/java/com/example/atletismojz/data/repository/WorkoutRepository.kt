package com.example.atletismojz.data.repository

import android.content.Context
import android.util.Log
import com.example.atletismojz.data.model.WorkoutSummary
import com.google.gson.Gson
import java.io.File

class WorkoutRepository(context: Context) {
    private val recordsDir = File(context.filesDir, "workout_records").apply {
        if (!exists()) mkdirs()
    }
    private val gson = Gson()

    @Synchronized
    fun saveWorkout(summary: WorkoutSummary) {
        try {
            val file = File(recordsDir, "workout_${summary.id}.json")
            val json = gson.toJson(summary)
            file.writeText(json)
            Log.d("WorkoutRepository", "Guardado entrenamiento ${summary.id} en ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error al guardar entrenamiento", e)
        }
    }

    @Synchronized
    fun getAllWorkouts(): List<WorkoutSummary> {
        val workouts = mutableListOf<WorkoutSummary>()
        try {
            val files = recordsDir.listFiles { file -> file.extension == "json" } ?: emptyArray()
            for (file in files) {
                try {
                    val json = file.readText()
                    val summary = gson.fromJson(json, WorkoutSummary::class.java)
                    if (summary != null) {
                        workouts.add(summary)
                    }
                } catch (e: Exception) {
                    Log.e("WorkoutRepository", "Error leyendo archivo ${file.name}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error obteniendo entrenamientos", e)
        }
        return workouts.sortedByDescending { it.timestamp }
    }

    @Synchronized
    fun deleteWorkout(id: String) {
        try {
            val file = File(recordsDir, "workout_$id.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error eliminando entrenamiento $id", e)
        }
    }

    @Synchronized
    fun clearAllWorkouts() {
        try {
            recordsDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error limpiando entrenamientos", e)
        }
    }
}
