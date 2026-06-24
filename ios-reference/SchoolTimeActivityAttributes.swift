import ActivityKit
import Foundation

struct SchoolTimeActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var subject: String
        var periodLabel: String
        var room: String
        var remainingMinutes: Int
        var nextSubject: String
        var nextStartTime: String
    }

    var timetableId: String
}
