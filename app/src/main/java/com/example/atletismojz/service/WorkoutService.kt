package com.example.atletismojz.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.atletismojz.R
import com.example.atletismojz.data.model.IntervalBlock
import com.example.atletismojz.data.model.IntervalConfig
import com.example.atletismojz.data.model.LocationPoint
import com.example.atletismojz.data.model.ManualSplit
import com.example.atletismojz.data.model.MultiBlockIntervalConfig
import com.example.atletismojz.data.model.WorkoutData
import com.example.atletismojz.data.model.WorkoutState
import com.example.atletismojz.data.model.WorkoutSummary
import com.example.atletismojz.data.model.WorkoutType
import com.example.atletismojz.data.repository.WorkoutRepository
import com.example.atletismojz.presentation.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutService : LifecycleService(), SensorEventListener {

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    private val binder = LocalBinder()

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var workoutRepository: WorkoutRepository
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    private var timerJob: Job? = null
    private var lastLocation: Location? = null
    private val hrReadings = mutableListOf<Int>()

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "workout_channel"
        private val _workoutData = MutableStateFlow(WorkoutData())
        val workoutData: StateFlow<WorkoutData> = _workoutData.asStateFlow()

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_TOGGLE_SPLIT = "ACTION_TOGGLE_SPLIT"
        const val ACTION_ADD_LAP = "ACTION_ADD_LAP"
    }

    override fun onCreate() {
        super.onCreate()
        workoutRepository = WorkoutRepository(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AtletismoJZ::WorkoutWakeLock")

        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                val typeName = intent.getStringExtra("WORKOUT_TYPE") ?: WorkoutType.CONTINUOUS_RUN.name
                val type = WorkoutType.valueOf(typeName)
                val workSec = intent.getIntExtra("WORK_SEC", 30)
                val restSec = intent.getIntExtra("REST_SEC", 30)
                val sets = intent.getIntExtra("SETS", 10)
                val targetTimeSec = intent.getLongExtra("TARGET_TIME_SEC", 0L)
                val targetDistMeters = intent.getDoubleExtra("TARGET_DIST_METERS", 0.0)

                startWorkout(
                    type = type,
                    config = IntervalConfig(workSec, restSec, sets),
                    targetTimeSec = targetTimeSec,
                    targetDistMeters = targetDistMeters
                )
            }
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> stopWorkout()
            ACTION_TOGGLE_SPLIT -> toggleManualSplit()
            ACTION_ADD_LAP -> addManualLap()
        }
        return START_STICKY
    }

    fun startWorkout(
        type: WorkoutType,
        config: IntervalConfig = IntervalConfig(),
        targetTimeSec: Long = 0L,
        targetDistMeters: Double = 0.0,
        multiBlockConfig: MultiBlockIntervalConfig = MultiBlockIntervalConfig()
    ) {
        val firstBlockWork = if (type == WorkoutType.MULTI_BLOCK_INTERVALS && multiBlockConfig.blocks.isNotEmpty()) {
            multiBlockConfig.blocks[0].workSeconds
        } else if (type == WorkoutType.INTERVALS) {
            config.workSeconds
        } else 0

        val initialData = WorkoutData(
            type = type,
            state = WorkoutState.RUNNING,
            intervalConfig = config,
            multiBlockConfig = multiBlockConfig,
            currentBlockIndex = 0,
            phaseTimeRemainingSec = firstBlockWork,
            isWorkPhase = true,
            currentSet = 1,
            targetTimeSeconds = targetTimeSec,
            targetDistanceMeters = targetDistMeters
        )
        _workoutData.value = initialData
        hrReadings.clear()
        lastLocation = null

        startForeground(NOTIFICATION_ID, buildNotification(initialData))
        wakeLock?.acquire(10 * 60 * 60 * 1000L)

        registerSensors()
        startLocationUpdates()
        startTimer()

        vibratePattern(longArrayOf(0, 200, 100, 200))
    }

    fun pauseWorkout() {
        if (_workoutData.value.state == WorkoutState.RUNNING) {
            _workoutData.update { it.copy(state = WorkoutState.PAUSED) }
            stopSensors()
            stopLocationUpdates()
            timerJob?.cancel()
            updateNotification(_workoutData.value)
            vibratePattern(longArrayOf(0, 250))
        }
    }

    fun resumeWorkout() {
        if (_workoutData.value.state == WorkoutState.PAUSED) {
            _workoutData.update { it.copy(state = WorkoutState.RUNNING) }
            registerSensors()
            startLocationUpdates()
            startTimer()
            updateNotification(_workoutData.value)
            vibratePattern(longArrayOf(0, 200))
        }
    }

    private var lastSplitToggleTime = 0L

    fun toggleManualSplit() {
        val now = System.currentTimeMillis()
        if (now - lastSplitToggleTime < 800L) return // Debounce para evitar doble cambio rápido
        lastSplitToggleTime = now

        val current = _workoutData.value
        if (current.state != WorkoutState.RUNNING) return

        val newSplit = ManualSplit(
            splitIndex = current.manualSplits.size + 1,
            isWorkPhase = current.isWorkPhase,
            durationSeconds = current.currentSplitDurationSec,
            distanceMeters = current.currentSplitDistanceMeters
        )

        val nextPhase = !current.isWorkPhase

        _workoutData.update {
            it.copy(
                manualSplits = it.manualSplits + newSplit,
                isWorkPhase = nextPhase,
                currentSplitDurationSec = 0L,
                currentSplitDistanceMeters = 0.0
            )
        }

        if (nextPhase) {
            vibratePattern(longArrayOf(0, 200, 100, 200, 100, 200))
        } else {
            vibratePattern(longArrayOf(0, 300, 100, 300))
        }
    }

    private val completionVibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500, 200, 800)

    fun stopWorkout(): WorkoutSummary? {
        val current = _workoutData.value
        if (current.state == WorkoutState.IDLE) return null

        timerJob?.cancel()
        stopSensors()
        stopLocationUpdates()

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        var finalSplits = current.manualSplits
        if (current.currentSplitDurationSec > 0 || current.currentSplitDistanceMeters > 0) {
            val lastSplit = ManualSplit(
                splitIndex = current.manualSplits.size + 1,
                isWorkPhase = current.isWorkPhase,
                durationSeconds = current.currentSplitDurationSec,
                distanceMeters = current.currentSplitDistanceMeters
            )
            finalSplits = finalSplits + lastSplit
        }

        val avgHr = if (hrReadings.isNotEmpty()) hrReadings.average().toInt() else current.avgHeartRate
        val avgPace = if (current.distanceMeters > 0 && current.totalDurationSeconds > 0) {
            (current.totalDurationSeconds / (current.distanceMeters / 1000.0))
        } else 0.0

        val compressedPolyline = com.example.atletismojz.data.util.PolylineUtils.encode(current.routePoints)

        val summary = WorkoutSummary(
            workoutType = current.type,
            durationSeconds = current.totalDurationSeconds,
            distanceMeters = current.distanceMeters,
            avgHeartRate = avgHr,
            maxHeartRate = current.maxHeartRate,
            avgPaceSecPerKm = avgPace,
            intervalConfig = if (current.type == WorkoutType.INTERVALS) current.intervalConfig else null,
            multiBlockConfig = if (current.type == WorkoutType.MULTI_BLOCK_INTERVALS) current.multiBlockConfig else null,
            totalSetsCompleted = if (current.type == WorkoutType.INTERVALS) current.currentSet else 0,
            manualSplits = finalSplits,
            encodedPolyline = compressedPolyline
        ).also { workoutRepository.saveWorkout(it) }

        _workoutData.update { it.copy(state = WorkoutState.FINISHED) }
        stopForeground(STOP_FOREGROUND_REMOVE)

        vibratePattern(completionVibrationPattern)

        return summary
    }

    fun addManualLap() {
        if (_workoutData.value.type == WorkoutType.MANUAL_SERIES) {
            toggleManualSplit()
        } else {
            _workoutData.update { current ->
                val newLaps = current.lapDistancesMeters + current.distanceMeters
                current.copy(
                    totalLaps = current.totalLaps + 1,
                    lapDistancesMeters = newLaps
                )
            }
            vibratePattern(longArrayOf(0, 150))
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L)
                val current = _workoutData.value
                if (current.state != WorkoutState.RUNNING) break

                val newTotalSec = current.totalDurationSeconds + 1
                val newSplitSec = current.currentSplitDurationSec + 1

                if (current.targetTimeSeconds > 0 && newTotalSec == current.targetTimeSeconds) {
                    vibratePattern(completionVibrationPattern)
                }

                if (current.type == WorkoutType.INTERVALS) {
                    var remaining = current.phaseTimeRemainingSec - 1
                    var isWork = current.isWorkPhase
                    var set = current.currentSet

                    if (remaining in 1..3) {
                        vibratePattern(longArrayOf(0, 80))
                    }

                    if (remaining <= 0) {
                        if (isWork) {
                            isWork = false
                            remaining = current.intervalConfig.restSeconds
                            vibratePattern(longArrayOf(0, 300, 100, 300))
                        } else {
                            if (set < current.intervalConfig.totalSets) {
                                set += 1
                                isWork = true
                                remaining = current.intervalConfig.workSeconds
                                vibratePattern(longArrayOf(0, 200, 100, 200, 100, 200))
                            } else {
                                launch(Dispatchers.Main) {
                                    stopWorkout()
                                }
                                break
                            }
                        }
                    }

                    _workoutData.update {
                        it.copy(
                            totalDurationSeconds = newTotalSec,
                            currentSplitDurationSec = newSplitSec,
                            phaseTimeRemainingSec = remaining,
                            isWorkPhase = isWork,
                            currentSet = set
                        )
                    }
                } else if (current.type == WorkoutType.MULTI_BLOCK_INTERVALS) {
                    val blocks = current.multiBlockConfig.blocks
                    val activeBlock = blocks.getOrNull(current.currentBlockIndex) ?: blocks.firstOrNull() ?: IntervalBlock()

                    var remaining = current.phaseTimeRemainingSec - 1
                    var isWork = current.isWorkPhase
                    var set = current.currentSet
                    var blockIdx = current.currentBlockIndex

                    if (remaining in 1..3) {
                        vibratePattern(longArrayOf(0, 80))
                    }

                    if (remaining <= 0) {
                        if (isWork) {
                            isWork = false
                            remaining = activeBlock.restSeconds
                            vibratePattern(longArrayOf(0, 300, 100, 300))
                        } else {
                            if (set < activeBlock.totalSets) {
                                set += 1
                                isWork = true
                                remaining = activeBlock.workSeconds
                                vibratePattern(longArrayOf(0, 200, 100, 200, 100, 200))
                            } else {
                                // Siguiente bloque
                                if (blockIdx + 1 < blocks.size) {
                                    blockIdx += 1
                                    val nextBlock = blocks[blockIdx]
                                    set = 1
                                    isWork = true
                                    remaining = nextBlock.workSeconds
                                    vibratePattern(longArrayOf(0, 400, 100, 400, 100, 400, 100, 400)) // 4 impulsos cambio de bloque
                                } else {
                                    launch(Dispatchers.Main) {
                                        stopWorkout()
                                    }
                                    break
                                }
                            }
                        }
                    }

                    _workoutData.update {
                        it.copy(
                            totalDurationSeconds = newTotalSec,
                            currentSplitDurationSec = newSplitSec,
                            phaseTimeRemainingSec = remaining,
                            isWorkPhase = isWork,
                            currentSet = set,
                            currentBlockIndex = blockIdx
                        )
                    }
                } else {
                    _workoutData.update {
                        it.copy(
                            totalDurationSeconds = newTotalSec,
                            currentSplitDurationSec = newSplitSec
                        )
                    }
                }

                if (newTotalSec % 5 == 0L) {
                    updateNotification(_workoutData.value)
                }
            }
        }
    }

    private fun registerSensors() {
        heartRateSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    private var lastHighBpmAlertTime = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values.getOrNull(0)?.toInt() ?: 0
            if (bpm > 0) {
                hrReadings.add(bpm)
                val avg = hrReadings.average().toInt()
                val max = maxOf(_workoutData.value.maxHeartRate, bpm)

                val now = System.currentTimeMillis()
                if (bpm >= 180 && (now - lastHighBpmAlertTime > 15_000L)) {
                    lastHighBpmAlertTime = now
                    vibratePattern(longArrayOf(0, 3000))
                }

                _workoutData.update {
                    it.copy(
                        currentHeartRate = bpm,
                        avgHeartRate = avg,
                        maxHeartRate = max
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startLocationUpdates() {
        try {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 2000L
            ).setMinUpdateIntervalMillis(1000L).build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            if (location.accuracy > 30) return

            val current = _workoutData.value
            if (current.state != WorkoutState.RUNNING) return

            var addedDist = 0.0
            lastLocation?.let { last ->
                addedDist = last.distanceTo(location).toDouble()
            }
            lastLocation = location

            val newTotalDistance = current.distanceMeters + addedDist
            val newSplitDistance = current.currentSplitDistanceMeters + addedDist

            val currentKm = (newTotalDistance / 1000.0).toInt()
            var newKmNoticeText = current.lastKmNoticeText
            var newLastKmCompleted = current.lastKmCompleted

            if (currentKm > current.lastKmCompleted && currentKm > 0) {
                newLastKmCompleted = currentKm
                newKmNoticeText = "¡Km $currentKm completado!"
                vibratePattern(longArrayOf(0, 300, 150, 300))
            }

            if (current.targetDistanceMeters > 0.0 && newTotalDistance >= current.targetDistanceMeters && current.distanceMeters < current.targetDistanceMeters) {
                vibratePattern(completionVibrationPattern)
            }

            val speedMetersPerSec = location.speed
            val paceSecPerKm = if (speedMetersPerSec > 0.5) (1000.0 / speedMetersPerSec) else 0.0
            val newPoint = LocationPoint(location.latitude, location.longitude, location.altitude)

            _workoutData.update { data ->
                data.copy(
                    distanceMeters = newTotalDistance,
                    currentSplitDistanceMeters = newSplitDistance,
                    currentPaceSecPerKm = paceSecPerKm,
                    routePoints = data.routePoints + newPoint,
                    lastKmCompleted = newLastKmCompleted,
                    lastKmNoticeText = newKmNoticeText
                )
            }
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Entrenamiento Atletismo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el progreso del entrenamiento activo"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(data: WorkoutData): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (data.type) {
            WorkoutType.INTERVALS, WorkoutType.MULTI_BLOCK_INTERVALS -> {
                "Cambios: ${if (data.isWorkPhase) "¡CORRER!" else "TROTAR"} (${data.phaseTimeRemainingSec}s)"
            }
            WorkoutType.MANUAL_SERIES -> {
                "Serie ${data.manualSplits.size + 1}: ${if (data.isWorkPhase) "CORRER" else "DESCANSO"}"
            }
            else -> {
                "Carrera: ${data.formattedDuration}"
            }
        }

        val contentText = "Pulsaciones: ${if (data.currentHeartRate > 0) "${data.currentHeartRate} BPM" else "--"} | ${data.formattedDistanceKm}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(data: WorkoutData) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(data))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        stopSensors()
        stopLocationUpdates()
    }
}
