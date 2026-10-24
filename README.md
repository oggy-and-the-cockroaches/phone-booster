# 🚀 Phone Booster (BoostMax)

An Android Performance Booster and Storage Analyzer application built with **Kotlin + Jetpack Compose** for the UI and **C++ (NDK)** for performance-critical native operations.

---

## ⚡ Key Highlights

- **C++ NDK Acceleration**:
  - Direct POSIX `/proc/` scanner for processes, memory, and CPU loads.
  - High-speed recursive filesystem scanning with min-heap largest file ranking.
  - Native junk & cache detection and purging.
  - Dual-engine architecture with automatic Kotlin/Android system API fallback.
- **Modern Jetpack Compose UI**:
  - Premium dark theme with glassmorphic cards and sweep-gradient circular gauges.
  - Micro-animations, live system polling, and custom typography.
  - Bottom navigation bar with smooth screen transitions.

---

## 📱 Features

1. **Dashboard**
   - Live animated gauges for **RAM Usage** and **CPU Load**.
   - Battery level, temperature (°C), and storage capacity metrics.
   - Real-time engine status indicator (`C++ NDK Active` / `Hybrid Engine`).
   - One-tap quick system boost.

2. **Process Manager**
   - Active process listing with app icons, PID, package names, RSS memory, and thread counts.
   - Search by app or package name.
   - Filter chips: *All*, *User Apps*, and *System*.
   - Sort by memory (descending/ascending), app name, or PID.
   - Terminate individual processes or tap **Stop All** to kill background user processes.

3. **Storage Analyzer**
   - Multi-segment storage bar: Images, Videos, Audio, Documents, Archives/APKs, and Other.
   - Storage metrics: Total, Used, and Free space.
   - **Large Files Finder**: Identifies the largest space-consuming files (> 500 KB / > 10 MB) with direct deletion support.
   - **App Storage Breakdown**: Detailed inspection of installed apps (App size, User Data, and Cache).

4. **Cache Cleaner**
   - Calculates reclaimable space from app caches, temporary files, thumbnails, obsolete APKs, and log files.
   - Multi-select item checkboxes with *Select All* / *Deselect All*.
   - One-tap junk purge with storage reclaimed summary dialog.

5. **RAM Booster**
   - Detailed RAM breakdown: Total, Available Free, Active Used, and Cached memory.
   - Top background RAM consumer apps.
   - Super Boost animation and statistics dialog.

---

## 🏗️ Architecture

```
app/
├── src/main/
│   ├── cpp/                         # C++ Native Code (NDK)
│   │   ├── CMakeLists.txt
│   │   ├── native-bridge.cpp        # JNI Bridge
│   │   ├── memory_profiler.cpp/.h   # /proc/meminfo reader
│   │   ├── process_scanner.cpp/.h   # /proc/ process scanner
│   │   ├── storage_analyzer.cpp/.h  # Recursive file analyzer
│   │   └── cache_scanner.cpp/.h     # Cache & junk cleaner
│   ├── java/com/example/myapplication/
│   │   ├── data/
│   │   │   ├── NativeBridge.kt      # JNI & Dual-Engine loader
│   │   │   ├── models/              # Process, Storage, Cache & System models
│   │   │   └── repository/          # Repositories for metrics, processes & storage
│   │   ├── viewmodel/               # ViewModels for each feature
│   │   ├── ui/
│   │   │   ├── theme/               # Dark theme, colors & typography
│   │   │   ├── components/          # Glassmorphic cards, gauges, buttons & bars
│   │   │   └── screens/             # 5 full feature screens
│   │   ├── navigation/              # NavHost & bottom navigation
│   │   └── MainActivity.kt
│   └── AndroidManifest.xml
```

---

## 🛠️ Build & Installation

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+ or JDK 21+
- Android SDK 31+ (Target SDK 37)

### Build APK via Gradle
```bash
# Debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
MIT License
