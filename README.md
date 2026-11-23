# 🏃‍♂️ AtletismoJZ - Wear OS Running & Athletics Trainer

**AtletismoJZ** is an advanced, open-source Wear OS running and athletics training application built with **Jetpack Compose for Wear OS (Material 3)**. Designed specifically for runners and track athletes using smartwatches (such as OnePlus Watch, Pixel Watch, Galaxy Watch), it provides robust background execution, GPS route tracking, heart rate monitoring, and specialized training modes.

---

## ✨ Features

- **Background Foreground Service**: Keeps the timer, GPS, and sensors active in the background without getting killed by screen timeouts or battery savers.
- **🏃‍♂️ Manual Split Series (Hardware Button Support)**:
  - Designed for track athletes. Press the watch's physical lower button to instantly toggle between **Run (Correr)** and **Rest/Jog (Descanso)**.
  - No need to look at the screen while running hard intervals!
- **🧱 Multi-Block Fartlek & Interval Training**:
  - Program up to 3 distinct interval blocks (e.g., Block 1: 30s/30s x5, Block 2: 45s/45s x4) with automatic haptic vibration alerts on phase and block transitions.
- **⏱️ Jogging Time & Precise Distance Targets**:
  - Configure target jogging time or track precise athletics distances (from 10m to 500m in 10m steps, up to 10km).
  - Rotary crown scrolling support for ultra-fast setup.
- **❤️ Real-Time Heart Rate Zones (Z1 - Z5)**:
  - Live color-coded heart rate zone indicator (Recovery, Easy, Aerobic, Threshold, Max) with a high-HR 3-second continuous vibration alert (>180 BPM).
- **🗺️ GPS Route & Polyline Compression**:
  - Real-time GPS distance, instantaneous pace (min/km), and compressed route rendering on watch canvas.
- **💾 Local Workout History (`WorkoutRepository`)**:
  - File-based individual JSON storage for past workouts with complete summary metrics and split breakdowns.
- **🔋 Always-On Screen Management**:
  - Keeps the screen active and bright during training sessions so you can glance at your stats instantly.

---

## 📥 Installation

1. Download the latest compiled APK from the [Releases](https://github.com/Juanillo121214/App-atletismo-correr-for-Wear-OS/releases) page.
2. Install it on your Wear OS watch using wireless ADB debugging or an APK sideloading app (such as Bugjaeger).

---

## 🛠️ Built With
- Kotlin & Jetpack Compose for Wear OS (Material 3)
- Android Location & Health Services (FusedLocationProviderClient, SensorManager)
- Coroutines & StateFlow
- Gson & Polyline Encoding
