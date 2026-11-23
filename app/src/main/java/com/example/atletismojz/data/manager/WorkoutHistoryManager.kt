package com.example.atletismojz.data.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.atletismojz.data.model.WorkoutSummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class WorkoutHistoryManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("atletismo_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyHistory = "key_workout_history"
    private val historyFile = File(context.filesDir, "workout_history.json")

    private val maxHistoryItems = 30

    @Synchronized
    fun saveWorkout(summary: WorkoutSummary) {
        try {
            val currentHistory = getHistory().toMutableList()
            currentHistory.add(0, summary)

            val trimmedHistory = if (currentHistory.size > maxHistoryItems) {
                currentHistory.take(maxHistoryItems)
            } else {
                currentHistory
            }

            val json = gson.toJson(trimmedHistory)
            prefs.edit().putString(keyHistory, json).apply()

            try {
                historyFile.writeText(json)
            } catch (e: Exception) {
                Log.e("WorkoutHistoryManager", "Error al escribir archivo local", e)
            }
        } catch (e: Exception) {
            Log.e("WorkoutHistoryManager", "Error al guardar entrenamiento", e)
        }
    }

    @Synchronized
    fun getHistory(): List<WorkoutSummary> {
        var json = prefs.getString(keyHistory, null)

        if (json.isNullOrEmpty() && historyFile.exists()) {
            try {
                json = historyFile.readText()
            } catch (e: Exception) {
                json = null
            }
        }

        if (json.isNullOrEmpty()) return emptyList()

        return try {
            val type = object : TypeToken<List<WorkoutSummary>>() {}.type
            val list: List<WorkoutSummary>? = gson.fromJson(json, type)
            list ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun clearHistory() {
        prefs.edit().remove(keyHistory).apply()
        if (historyFile.exists()) {
            try {
                historyFile.delete()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
