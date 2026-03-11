# Background work and power (Doze, battery optimization)

**Purpose:** Document whether the app requests "ignore battery optimizations" and how Doze affects background work. Ref: Blind Spot Plan §8.

---

## Ignore battery optimizations

- The app **does not** request "ignore battery optimizations" (no `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` or similar). Foreground location and foreground services run with normal power constraints.
- **BatteryOptimizationService** and **SettingsManager** (`isBatteryOptimizationEnabled`) refer to an **in-app setting** that adjusts GPS update frequency (e.g. reduce frequency when battery is low or when stationary). This is not the system-level "battery optimization" exemption.

---

## Doze and App Standby

- **WorkManager** is used for periodic sync (e.g. `SyncWorker`). WorkManager jobs are subject to **Doze** and **App Standby**. On Doze, execution can be **deferred** until a maintenance window or when the device leaves Doze. See [Android background work](https://developer.android.com/topic/architecture/workmanager/advanced/dowork#deferrability) and [Doze](https://developer.android.com/training/monitoring-device-state/doze-standby).
- **Trip-ended detection** and **periodic sync** may therefore run later than scheduled on some devices when the app is in the background and the device is in Doze. This is expected; no change to policy is documented here.

---

## Optional manual test

- Test with battery optimization (system) on and off; test with Doze simulated (e.g. `adb shell dumpsys battery unplug` + `adb shell dumpsys deviceidle force-idle`) and confirm expected behavior (e.g. sync runs when device wakes or in maintenance window).

---

*Ref: [SyncWorker](../app/src/main/java/com/example/outofroutebuddy/workers/SyncWorker.kt), [BatteryOptimizationService](../app/src/main/java/com/example/outofroutebuddy/services/BatteryOptimizationService.kt).*
