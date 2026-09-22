# Volume Control & Lock

Production-oriented Android app for software volume control and optional volume locking.

## Stack
- Kotlin, Jetpack Compose, Material 3
- minSdk 26, target/compile SDK 36
- Preferences DataStore
- Android AccessibilityService for supported volume-key filtering
- Foreground overlay service using SYSTEM_ALERT_WINDOW

## Features
- Media-first volume dashboard and stream selector
- Volume slider, up/down, mute/unmute
- Lock/unlock selected percentage
- Optional accessibility volume-key compensation
- Optional floating controller
- Local preferences
- Boot preference where Android permits it
- No analytics, ads, network, or unnecessary personal permissions

## Android limitations
A normal app cannot guarantee a universal system-level volume lock. OEMs and Android versions can bypass accessibility key filtering or change volume behavior. The service restores the configured level when the event is delivered. Call and notification stream behavior can also be restricted by the OS/device policy.

## Build
GitHub Actions builds the debug APK in the cloud.
APK output: `app/build/outputs/apk/debug/app-debug.apk`

No signing credentials are included.

Build workflow trigger: automated debug APK build and artifact upload are enabled on pushes to `main`.
