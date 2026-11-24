# 🏃‍♂️ AtletismoJZ - Technical Architecture & Complete App Summary (Wear OS)

This document provides a comprehensive technical overview of the **AtletismoJZ** Wear OS application for AI agents, developers, and maintainers. It details the complete architecture, data flow, state management, hardware input handling, storage mechanisms, and file structure.

---

## 🛠️ Tech Stack & Platform Specs

- **Platform**: Wear OS (Standalone App: `com.google.android.wearable.standalone = true`)
- **Min SDK**: 30 (Android 11) | **Target SDK**: 37 | **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose for Wear OS Material 3 (`androidx.wear.compose:compose-material3:1.5.6`, `compose-foundation:1.5.6`)
- **Build System**: Android Gradle Plugin 9.4.1 with Kotlin Compose Plugin
- **Target Hardware**: OnePlus Watch 3 (Dual Engine: BES2700 + Snapdragon W5), Pixel Watch, Samsung Galaxy Watch, and standard Wear OS devices.

---

## 🚀 Core Features & Workout Modes

The app supports **6 specialized athletics & running workout modes**:

1. **`CONTINUOUS_RUN` (Carrera Continua)**: Standard outdoor/indoor run with continuous timer, GPS tracking, and heart rate monitor.
2. **`MANUAL_SERIES` (Series Manuales por Botón Físico/Táctil)**: Designed for track & field athletes. Pressing the lower physical hardware button or tapping the watch screen toggles between **CORRER (Run)** and **DESCANSO (Rest/Jog)**. Each split records its exact duration, distance, and pace.
3. **`JOGGING` (Trote por Tiempo)**: Target duration workout (e.g. 15, 20, 30, 45, 60 minutes). Displays countdown target time and triggers a celebration vibration when reached.
4. **`INTERVALS` (Cambios de Ritmo 1 Bloque)**: Single-block interval training (Work sec / Rest sec x Sets) with count-down warning ticks (3, 2, 1s) and phase transition vibration alerts.
5. **`MULTI_BLOCK_INTERVALS` (Cambios Multi-Bloque)**: Pyramid/structured fartlek supporting up to **3 independent blocks** (e.g., Block 1: 30s/30s x5; Block 2: 45s/45s x4; Block 3: 60s/30s x3) with automatic block transitions.
6. **`DISTANCE_TARGET` (Objetivo Distancia / Series de Pista)**: Precise track distance goals from **10m to 500m in 10m increments**, and from 500m up to 10,000m, adjustable via the **Rotary Crown**.

---

## ⚡ Hardware Input & Event Dispatching Architecture

### 1. Hardware Lower Button Interception
- In `MainActivity.kt`, key events are intercepted at the window level using `@Suppress("RestrictedApi") override fun dispatchKeyEvent(event: KeyEvent)` and `OnBackPressedCallback`.
- It catches secondary stem buttons (`KEYCODE_STEM_1`, `KEYCODE_STEM_2`, `KEYCODE_STEM_3`, `KEYCODE_STEM_PRIMARY`, `KEYCODE_BUTTON_1`..`4`, `KEYCODE_NAVIGATE_PREVIOUS`) and system `KEYCODE_BACK`.
- When a workout is active (`RUNNING` or `PAUSED`), hardware button presses execute `triggerPhysicalButtonAction()`.
- **Mode-Specific Physical Button Behavior**:
  - `MANUAL_SERIES`: Switches between Correr ↔ Trotar/Descanso (`toggleManualSplit()`).
  - **All other modes**: Pauses active workout (`pauseWorkout()`) if `RUNNING`, or resumes workout (`resumeWorkout()`) if `PAUSED`.

### 2. 1-Touch Instant Screen Interaction (`detectTapGestures` & `Card.onClick`)
- `ActiveWorkoutScreen.kt` wraps the entire screen in `pointerInput(Unit) { detectTapGestures(onTap = { onAddLap() }) }` AND includes an interactive Wear Compose M3 `Card(onClick = onAddLap)`.
- This ensures 100% instant 1-touch response on the very first touch without Compose container focus delays.

### 3. 800ms Debounce Protection
- `WorkoutService.kt` includes an 800ms timestamp filter (`lastSplitToggleTime`) inside `toggleManualSplit()` to prevent double-toggling bugs.

### 4. Rotary Crown Navigation
- Setup screens use `Modifier.focusRequester(focusRequester)`, `focusable()`, and `onRotaryScrollEvent { event -> ... }` to allow smooth hardware crown rotation for scrolling lists and selecting precise distances.

---

## 🔋 Power, Screen & Background Execution Architecture

1. **Foreground Service (`WorkoutService`)**:
   - Runs as a `LifecycleService` with `FOREGROUND_SERVICE_LOCATION` and `FOREGROUND_SERVICE_HEALTH` permissions.
   - Shows an ongoing notification on channel `workout_channel` (ID `1001`).
   - Holds a `PowerManager.WakeLock` (`PARTIAL_WAKE_LOCK`) to guarantee CPU execution for GPS and timer in the background.

2. **Screen Always-On Management (`setKeepScreenAwake`)**:
   - When a workout is `RUNNING` or `PAUSED`, `MainActivity` applies `FLAG_KEEP_SCREEN_ON`, `setShowWhenLocked(true)`, and `setTurnScreenOn(true)`.
   - The screen stays active and responsive throughout the workout without dimming or exiting to the watch face launcher after 60s.
   - When the workout finishes, `setKeepScreenAwake(false)` clears the flags, immediately restoring the user's default system Always-On Display (AOD) settings.

---

## ❤️ Heart Rate Zones & GPS Compression Architecture

1. **Heart Rate Zones (Z1 - Z5)**:
   - Evaluated dynamically in `WorkoutData.heartRateZone` based on current BPM vs. max HR (190 BPM default):
     - **Z1 - Recovery (Blue)**: < 60%
     - **Z2 - Easy/Jog (Green)**: 60 - 70%
     - **Z3 - Aerobic (Yellow)**: 70 - 80%
     - **Z4 - Threshold (Orange)**: 80 - 90%
     - **Z5 - Max Effort (Red)**: > 90%
   - **High HR Alert**: When BPM >= 180, triggers a **3-second continuous vibration alert** (`longArrayOf(0, 3000)`).

2. **1 KM Auto-Split Alert**:
   - Automatically detects every 1,000m completed and triggers a double-pulse vibration.

3. **Google Polyline Route Compression (`PolylineUtils`)**:
   - Encodes thousands of lat/lng points into a single compact Polyline String (e.g. 100 KB JSON down to 2.5 KB string).
   - `WorkoutSummaryScreen` decodes the polyline on-the-fly and renders the route on a custom Compose `Canvas`.

---

## 💾 Local File-Based Storage Architecture (`WorkoutRepository`)

- **Location**: `context.filesDir/workout_records/`
- **Mechanism**: Stores each completed workout as an individual JSON file (`workout_<id>.json`).
- **Benefits**:
  - Completely immune to `SharedPreferences` string length/transaction limits.
  - Asynchronous and safe file I/O with fallback reading.
  - Automatically loads in `HistoryScreen.kt` via `LaunchedEffect(Unit)`.

---

## 📂 Project File Structure Directory

```
AtletismoJZ/
├── app/
│   ├── build.gradle.kts (AGP 9.4.1, Compose BOM 2024.09.00, Wear M3 1.5.6)
│   └── src/main/
│       ├── AndroidManifest.xml (Permissions: BODY_SENSORS, LOCATION, FOREGROUND_SERVICE, WAKE_LOCK, VIBRATE)
│       └── java/com/example/atletismojz/
│           ├── data/
│           │   ├── manager/
│           │   │   └── WorkoutHistoryManager.kt
│           │   ├── model/
│           │   │   ├── IntervalConfig.kt
│           │   │   ├── LocationPoint.kt
│           │   │   ├── ManualSplit.kt
│           │   │   ├── MultiBlockIntervalConfig.kt
│           │   │   ├── WorkoutData.kt
│           │   │   ├── WorkoutState.kt
│           │   │   ├── WorkoutSummary.kt
│           │   │   └── WorkoutType.kt
│           │   ├── repository/
│           │   │   └── WorkoutRepository.kt
│           │   └── util/
│           │       └── PolylineUtils.kt
│           ├── presentation/
│           │   ├── MainActivity.kt (Key event dispatching, window flags, navigation)
│           │   ├── screens/
│           │   │   ├── ActiveWorkoutScreen.kt (Live dashboard, tap gestures)
│           │   │   ├── DistanceSetupScreen.kt (10m-10km rotary picker)
│           │   │   ├── HistoryScreen.kt (Past workouts list)
│           │   │   ├── HomeScreen.kt (Main menu)
│           │   │   ├── IntervalSetupScreen.kt (1-block setup)
│           │   │   ├── JoggingSetupScreen.kt (Time target setup)
│           │   │   ├── MultiBlockSetupScreen.kt (3-block setup)
│           │   │   ├── PermissionGate.kt (Wear OS permissions handler)
│           │   │   └── WorkoutSummaryScreen.kt (Summary metrics & GPS canvas)
│           │   └── theme/
│           │       └── Theme.kt
│           └── service/
│               └── WorkoutService.kt (Foreground service, location, HR, vibration, splits)
```

---

## 📝 Instructions for Future AI Agents

1. **State Flow**: Always observe `WorkoutService.workoutData` state flow reactively.
2. **Adding Workout Modes**: Register new enum entry in `WorkoutType.kt`, handle timer logic in `WorkoutService.kt`, add setup screen in `screens/`, and add menu item in `HomeScreen.kt`.
3. **Key Events**: Keep window key dispatching in `MainActivity.kt` using `triggerPhysicalButtonAction()`.
4. **Storage**: Always save workouts via `WorkoutRepository` and compress GPS routes using `PolylineUtils.encode()`.
