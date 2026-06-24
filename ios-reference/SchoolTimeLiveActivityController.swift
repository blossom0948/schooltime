import ActivityKit
import Foundation

@available(iOS 16.2, *)
final class SchoolTimeLiveActivityController {
    private var activity: Activity<SchoolTimeActivityAttributes>?

    func start(subject: String, periodLabel: String, room: String, remainingMinutes: Int, nextSubject: String, nextStartTime: String) async throws {
        let attributes = SchoolTimeActivityAttributes(timetableId: "default")
        let state = SchoolTimeActivityAttributes.ContentState(
            subject: subject,
            periodLabel: periodLabel,
            room: room,
            remainingMinutes: remainingMinutes,
            nextSubject: nextSubject,
            nextStartTime: nextStartTime
        )
        activity = try Activity.request(attributes: attributes, content: .init(state: state, staleDate: nil), pushType: nil)
    }

    func update(subject: String, periodLabel: String, room: String, remainingMinutes: Int, nextSubject: String, nextStartTime: String) async {
        let state = SchoolTimeActivityAttributes.ContentState(
            subject: subject,
            periodLabel: periodLabel,
            room: room,
            remainingMinutes: remainingMinutes,
            nextSubject: nextSubject,
            nextStartTime: nextStartTime
        )
        await activity?.update(.init(state: state, staleDate: nil))
    }

    func stop() async {
        await activity?.end(nil, dismissalPolicy: .immediate)
        activity = nil
    }
}
