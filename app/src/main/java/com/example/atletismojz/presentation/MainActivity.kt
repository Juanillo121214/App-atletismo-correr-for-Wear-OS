package com.example.atletismojz.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.atletismojz.data.model.IntervalConfig
import com.example.atletismojz.data.model.MultiBlockIntervalConfig
import com.example.atletismojz.data.model.WorkoutState
import com.example.atletismojz.data.model.WorkoutSummary
import com.example.atletismojz.data.model.WorkoutType
import com.example.atletismojz.presentation.screens.ActiveWorkoutScreen
import com.example.atletismojz.presentation.screens.DistanceSetupScreen
import com.example.atletismojz.presentation.screens.HistoryScreen
import com.example.atletismojz.presentation.screens.HomeScreen
import com.example.atletismojz.presentation.screens.IntervalSetupScreen
import com.example.atletismojz.presentation.screens.JoggingSetupScreen
import com.example.atletismojz.presentation.screens.MultiBlockSetupScreen
import com.example.atletismojz.presentation.screens.PermissionGate
import com.example.atletismojz.presentation.screens.WorkoutSummaryScreen
import com.example.atletismojz.presentation.theme.AtletismoJZTheme
import com.example.atletismojz.service.WorkoutService

enum class Screen {
    HOME,
    JOGGING_SETUP,
    INTERVAL_SETUP,
    MULTI_BLOCK_SETUP,
    DISTANCE_SETUP,
    ACTIVE_WORKOUT,
    WORKOUT_SUMMARY,
    HISTORY
}

class MainActivity : ComponentActivity() {

    private var workoutService: WorkoutService? = null
    private var isBound = false
    private var lastPhysicalButtonTriggerTime = 0L

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            triggerPhysicalButtonAction()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WorkoutService.LocalBinder
            workoutService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            workoutService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        val serviceIntent = Intent(this, WorkoutService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            AtletismoJZTheme {
                PermissionGate {
                    MainNavigation(
                        onStartWorkout = { type, config, targetTimeSec, targetDistMeters ->
                            val intent = Intent(this, WorkoutService::class.java).apply {
                                action = WorkoutService.ACTION_START
                                putExtra("WORKOUT_TYPE", type.name)
                                putExtra("WORK_SEC", config.workSeconds)
                                putExtra("REST_SEC", config.restSeconds)
                                putExtra("SETS", config.totalSets)
                                putExtra("TARGET_TIME_SEC", targetTimeSec)
                                putExtra("TARGET_DIST_METERS", targetDistMeters)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                        },
                        onStartMultiBlockWorkout = { multiBlockConfig ->
                            workoutService?.startWorkout(
                                type = WorkoutType.MULTI_BLOCK_INTERVALS,
                                multiBlockConfig = multiBlockConfig
                            )
                        },
                        onPauseWorkout = {
                            workoutService?.pauseWorkout()
                        },
                        onResumeWorkout = {
                            workoutService?.resumeWorkout()
                        },
                        onStopWorkout = {
                            workoutService?.stopWorkout()
                        },
                        onAddLap = {
                            triggerPhysicalButtonAction()
                        },
                        onUpdateState = { state, _ ->
                            val isRunning = (state == WorkoutState.RUNNING || state == WorkoutState.PAUSED)
                            backPressedCallback.isEnabled = isRunning
                            setKeepScreenAwake(isRunning)
                        }
                    )
                }
            }
        }
    }

    fun triggerPhysicalButtonAction() {
        val now = System.currentTimeMillis()
        if (now - lastPhysicalButtonTriggerTime < 800L) return
        lastPhysicalButtonTriggerTime = now

        val currentData = WorkoutService.workoutData.value
        when (currentData.state) {
            WorkoutState.RUNNING -> {
                if (currentData.type == WorkoutType.MANUAL_SERIES) {
                    if (workoutService != null) {
                        workoutService?.toggleManualSplit()
                    } else {
                        sendServiceAction(WorkoutService.ACTION_TOGGLE_SPLIT)
                    }
                } else {
                    if (workoutService != null) {
                        workoutService?.pauseWorkout()
                    } else {
                        sendServiceAction(WorkoutService.ACTION_PAUSE)
                    }
                }
            }
            WorkoutState.PAUSED -> {
                if (workoutService != null) {
                    workoutService?.resumeWorkout()
                } else {
                    sendServiceAction(WorkoutService.ACTION_RESUME)
                }
            }
            else -> {}
        }
    }

    private fun sendServiceAction(actionName: String) {
        val actionIntent = Intent(this, WorkoutService::class.java).apply {
            action = actionName
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(actionIntent)
        } else {
            startService(actionIntent)
        }
    }

    fun setKeepScreenAwake(keepAwake: Boolean) {
        if (keepAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(false)
                setTurnScreenOn(false)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        val currentData = WorkoutService.workoutData.value
        if (currentData.state == WorkoutState.RUNNING || currentData.state == WorkoutState.PAUSED) {
            setKeepScreenAwake(true)
        }
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val currentData = WorkoutService.workoutData.value
        if (currentData.state == WorkoutState.RUNNING || currentData.state == WorkoutState.PAUSED) {
            val code = event.keyCode
            if (code != KeyEvent.KEYCODE_HOME && code != KeyEvent.KEYCODE_POWER) {
                triggerPhysicalButtonAction()
                return true
            }
        }
        return window.superDispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        setKeepScreenAwake(false)
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}

@Composable
fun MainNavigation(
    onStartWorkout: (type: WorkoutType, config: IntervalConfig, targetTimeSec: Long, targetDistMeters: Double) -> Unit,
    onStartMultiBlockWorkout: (MultiBlockIntervalConfig) -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onStopWorkout: () -> WorkoutSummary?,
    onAddLap: () -> Unit,
    onUpdateState: (WorkoutState, WorkoutType) -> Unit
) {
    val workoutData by WorkoutService.workoutData.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var lastSummary by remember { mutableStateOf<WorkoutSummary?>(null) }

    LaunchedEffect(workoutData.state, workoutData.type) {
        onUpdateState(workoutData.state, workoutData.type)
    }

    if (workoutData.state == WorkoutState.RUNNING || workoutData.state == WorkoutState.PAUSED) {
        if (currentScreen != Screen.ACTIVE_WORKOUT) {
            currentScreen = Screen.ACTIVE_WORKOUT
        }
    }

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                onStartContinuousRun = {
                    onStartWorkout(WorkoutType.CONTINUOUS_RUN, IntervalConfig(), 0L, 0.0)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onStartManualSeries = {
                    onStartWorkout(WorkoutType.MANUAL_SERIES, IntervalConfig(), 0L, 0.0)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onOpenJoggingSetup = {
                    currentScreen = Screen.JOGGING_SETUP
                },
                onOpenIntervalSetup = {
                    currentScreen = Screen.INTERVAL_SETUP
                },
                onOpenMultiBlockSetup = {
                    currentScreen = Screen.MULTI_BLOCK_SETUP
                },
                onOpenDistanceSetup = {
                    currentScreen = Screen.DISTANCE_SETUP
                },
                onOpenHistory = {
                    currentScreen = Screen.HISTORY
                }
            )
        }

        Screen.JOGGING_SETUP -> {
            JoggingSetupScreen(
                onStartJoggingWorkout = { minutes ->
                    onStartWorkout(WorkoutType.JOGGING, IntervalConfig(), minutes * 60L, 0.0)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.INTERVAL_SETUP -> {
            IntervalSetupScreen(
                onStartIntervalWorkout = { config ->
                    onStartWorkout(WorkoutType.INTERVALS, config, 0L, 0.0)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.MULTI_BLOCK_SETUP -> {
            MultiBlockSetupScreen(
                onStartMultiBlockWorkout = { config ->
                    onStartMultiBlockWorkout(config)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.DISTANCE_SETUP -> {
            DistanceSetupScreen(
                onStartDistanceWorkout = { targetMeters ->
                    onStartWorkout(WorkoutType.DISTANCE_TARGET, IntervalConfig(), 0L, targetMeters)
                    currentScreen = Screen.ACTIVE_WORKOUT
                },
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.ACTIVE_WORKOUT -> {
            ActiveWorkoutScreen(
                workoutData = workoutData,
                onPauseWorkout = onPauseWorkout,
                onResumeWorkout = onResumeWorkout,
                onStopWorkout = {
                    val summary = onStopWorkout()
                    if (summary != null) {
                        lastSummary = summary
                        currentScreen = Screen.WORKOUT_SUMMARY
                    } else {
                        currentScreen = Screen.HOME
                    }
                },
                onAddLap = onAddLap,
                onBackToHome = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.WORKOUT_SUMMARY -> {
            lastSummary?.let { summary ->
                WorkoutSummaryScreen(
                    summary = summary,
                    onDone = {
                        currentScreen = Screen.HOME
                    }
                )
            } ?: run {
                currentScreen = Screen.HOME
            }
        }

        Screen.HISTORY -> {
            HistoryScreen(
                onSelectWorkout = { summary ->
                    lastSummary = summary
                    currentScreen = Screen.WORKOUT_SUMMARY
                },
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }
    }
}
