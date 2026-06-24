import ActivityKit
import SwiftUI
import WidgetKit

struct SchoolTimeLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: SchoolTimeActivityAttributes.self) { context in
            LockScreenLiveActivityView(state: context.state)
                .activityBackgroundTint(Color(.systemBackground))
                .activitySystemActionForegroundColor(.orange)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(context.state.subject)
                            .font(.headline)
                        Text(context.state.periodLabel)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("\(context.state.remainingMinutes)m")
                        .font(.title3.bold())
                        .foregroundStyle(.orange)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    HStack {
                        Text("다음")
                            .font(.caption.bold())
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.orange.opacity(0.15), in: Capsule())
                        Text("\(context.state.nextSubject) \(context.state.nextStartTime)")
                            .font(.caption)
                        Spacer()
                    }
                }
            } compactLeading: {
                Text(context.state.subject.prefix(2))
                    .font(.caption.bold())
            } compactTrailing: {
                Text("\(context.state.remainingMinutes)m")
                    .font(.caption.bold())
                    .foregroundStyle(.orange)
            } minimal: {
                Text("\(context.state.remainingMinutes)")
                    .font(.caption2.bold())
                    .foregroundStyle(.orange)
            }
        }
    }
}

private struct LockScreenLiveActivityView: View {
    let state: SchoolTimeActivityAttributes.ContentState

    var body: some View {
        HStack(spacing: 12) {
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.orange)
                .frame(width: 52, height: 52)
                .overlay(Text("\(state.remainingMinutes)m").font(.headline.bold()).foregroundStyle(.white))
            VStack(alignment: .leading, spacing: 4) {
                Text(state.subject)
                    .font(.headline)
                Text("\(state.periodLabel) · \(state.room)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text("다음: \(state.nextSubject) \(state.nextStartTime)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding()
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 22))
    }
}
