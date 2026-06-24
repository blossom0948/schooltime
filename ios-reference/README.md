# iOS Reference Integration

These files are reference code for a future native iOS target. They do not build inside the current Android Gradle project.

Required Xcode setup:

- Add an iOS app target and a Widget Extension target.
- Enable `Live Activities` in the app target capabilities.
- Enable an App Group such as `group.com.blossom.schooltime` for the app and widget extension.
- Add `NSSupportsLiveActivities` = `YES` to the app target `Info.plist`.
- Share timetable snapshots through the App Group `UserDefaults` keys used in `SchoolTimeWidget.swift`.

Files:

- `SchoolTimeActivityAttributes.swift`: shared ActivityKit state model.
- `SchoolTimeLiveActivityWidget.swift`: Lock Screen and Dynamic Island UI.
- `SchoolTimeLiveActivityController.swift`: start, update, and stop helpers.
- `SchoolTimeWidget.swift`: WidgetKit home and lock screen widget reference.
