# SchoolTime

SchoolTime is an Android timetable app designed for Galaxy lock screens.

## What it does

- Register a Monday-Friday weekly calendar timetable.
- Show the current or next class in the app.
- Keep an ongoing lock-screen notification that shows the next class.
- Expand the notification to see the full timetable for today.
- Tap a class card to edit it from a rounded bottom sheet.
- Use the orange weekly grid to scan the full school week at once.
- Run a foreground real-time updater for current class and remaining time.
- Add Android widgets for compact current class and today's schedule.
- Keep iOS ActivityKit and WidgetKit reference code in `ios-reference/`.

## About Galaxy Now Bar

Samsung's Now Bar is shown at the bottom of the Galaxy lock screen for supported live activities. As of this app's first version, ordinary third-party apps cannot reliably place a fully custom card there on every Galaxy device. This app uses the closest currently usable Android path: a public, ongoing lock-screen notification.

If Samsung/Android exposes general Live Updates or Now Bar integration for this device, the existing notification and schedule calculation layer can be adapted to that API.

This project now uses Android ongoing notifications and Android 16 `Notification.ProgressStyle` when available. On Samsung phones, Now Bar exposure still depends on One UI support and Samsung's system policy. Standard Android home widgets are included; lock-screen/AOD placement depends on the launcher or OEM lock-screen widget support.

If the app does not appear in Samsung Now Bar settings, it means the current One UI build is not exposing this app as a Now Bar supported app. In that case, keep notification permission and lock-screen notifications enabled; the app will still show the real-time ongoing notification and widgets.

## Better feature ideas

- A widget for the home screen and lock screen that mirrors the next class.
- A quick import screen for copying a timetable from text or a photo.
- Class-end countdown, vibration before the bell, and lunch-time display.
- Different timetables for exam weeks or alternating A/B weeks.
- Subject colors and icons so the notification can be scanned faster.

## Build

Open the folder in Android Studio, let Gradle sync, then run the `app` configuration.

The project uses:

- Android Gradle Plugin 8.10.1
- `compileSdk` 36
- Java source code only, no external dependencies
