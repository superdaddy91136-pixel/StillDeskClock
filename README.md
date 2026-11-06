# Still — Minimalist OLED Desk Clock

**Still — Desk Clock** (`com.still.deskclock`) is a lightweight, native Android application written in **Kotlin** with **Jetpack Compose**, designed to run on a phone placed horizontally on a study desk.

---

## 1. Visual Design & Core Principles

- **Exact OLED Colour Palette**:
  - **Main background (`#000000`)**: Pure black covering the outer margins and majority of the screen to minimise OLED power draw and light bleed.
  - **Main display panel (`#151515`)**: Single restrained dark-charcoal rounded surface behind the main digits.
  - **Main digits (`#F2F2F2`)**: Soft white oversized numerals with tabular font spacing (`fontFeatureSettings = "tnum"`) so digits never shift width during live updates.
  - **Secondary text (`#A0A0A0`)**: Date, weekday, status indicators, and recorded lap times.
  - **Secondary controls (`#242424`)**: Unobtrusive pill controls with accessible touch targets (`>= 48dp` height).
  - **Subtle page indicators (`#555555`)**: Three compact bottom indicators showing the active mode.
- **Responsive Landscape & Portrait Proportions**:
  - Uses `BoxWithConstraints` to dynamically size the main numerals to occupy ~78–84% of the usable display width in landscape orientation and reflow cleanly in portrait orientation without text clipping.
- **Immersive Edge-to-Edge Display**:
  - Hides Android status and navigation bars via `WindowInsetsControllerCompat` (`BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`) and supports `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` on Android 9+ (API 28+).

---

## 2. Three Modes & Swipe Navigation

Horizontal swipe navigation (`HorizontalPager` with `beyondBoundsPageCount = 2`) lets users switch seamlessly between the three modes without resetting or interrupting any active session:

1. **Mode 1 — Digital Clock (`DeskClockScreen.kt`)**:
   - Default launch mode.
   - Displays the current date (`DD/MM/YY`) at top-left, full weekday name (`Saturday`) at top-right, and oversized 24-hour time (`HH mm`, no seconds) in the centre.
   - Listens to `ACTION_TIME_TICK`, `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED`, and `ACTION_DATE_CHANGED` broadcasts plus exact minute-boundary wake-ups, avoiding high-frequency recomposition.
2. **Mode 2 — Countdown Timer (`CountdownTimerScreen.kt` & `CountdownTimerViewModel.kt`)**:
   - Quick presets (`15m`, `30m`, `45m`, `60m`, `90m`) and a compact **Custom Duration** dialog supporting custom hours and minutes (e.g., `30m`, `75m`, `1h 30m`).
   - Displays `MM:SS` for durations under 1 hour and `HH:MM:SS` for 1 hour or longer.
   - Controls: **Start**, **Pause**, **Resume**, **Reset**, **+5m** (Add 5 minutes), and **−5m** (Subtract 5 minutes, clamped to prevent negative or zero durations).
   - Completion triggers a subtle `Time Up` state, a short local tone (`ToneGenerator` on `STREAM_ALARM`), and a gentle haptic waveform (`VibrationEffect.createWaveform`, Android 9 API 28+ compatible).
3. **Mode 3 — Stopwatch (`StopwatchScreen.kt` & `StopwatchViewModel.kt`)**:
   - Displays elapsed time with hundredths (`MM:SS.CC`, or `HH:MM:SS.CC` for multi-hour sessions).
   - Controls: **Start**, **Pause**, **Resume**, **Reset**, and **Lap**.
   - Shows recorded lap splits (`#1 00:12.48`) in a restrained horizontal strip at the top of the panel so the main digits remain dominant.

---

## 3. Timekeeping Correctness, Burn-In Protection & Screen-On Behaviour

- **Monotonic Timekeeping**:
  - Both `CountdownTimerViewModel` and `StopwatchViewModel` compute durations using `SystemClock.elapsedRealtime()` rather than decrementing/incrementing counters on each tick.
  - Essential session state is persisted to local `SharedPreferences` so active or paused sessions recover accurately across configuration changes, mode switches, or app restarts.
- **OLED Burn-In Protection (`BurnInProtection.kt`)**:
  - Shifts the display panel and typography together every 3 minutes along a deterministic, bounded 9-point path within `[-4.dp, +4.dp]` horizontally and `[-3.dp, +3.dp]` vertically (2–3 dp per step) while the activity is resumed.
- **Screen-On Lifecycle Management (`MainActivity.kt`)**:
  - Applies `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` in `onResume()` and clears it in `onPause()`—keeping the screen awake on the desk without holding a permanent background wake lock.

---

## 4. Technical Requirements & Build Instructions

### Requirements
- **Minimum SDK**: API 28 (Android 9 Pie)
- **Target / Compile SDK**: API 34 (Android 14)
- **Android Gradle Plugin**: `8.5.2`
- **Kotlin**: `1.9.24` (Compose Compiler `1.5.14`, Compose BOM `2024.06.00`)
- **JDK**: Java 17 (recommended for AGP 8.5.2)

### Building in Android Studio
1. Extract `StillDeskClock-AndroidProject.zip` and open the `StillDeskClock` directory in **Android Studio** (Jellyfish / Koala / Ladybug or newer).
2. Allow Android Studio to sync Gradle and download the Android SDK Platform 34 and Compose dependencies.
3. Select the `app` run configuration and click **Run** (or **Build > Build Bundle(s) / APK(s) > Build APK(s)**).

### Building from the Command Line
Ensure `ANDROID_HOME` (or `local.properties` with `sdk.dir=/path/to/Android/Sdk`) and **JDK 17** are configured, then run:

```bash
./gradlew assembleDebug
```

The generated debug APK will be written to:
```text
app/build/outputs/apk/debug/app-debug.apk
```

To run the unit tests for time formatting and burn-in offset bounds:
```bash
./gradlew testDebugUnitTest
```
