import SwiftUI
import WidgetKit

struct SchoolTimeEntry: TimelineEntry {
    let date: Date
    let subject: String
    let remaining: String
    let todayRows: [String]
}

struct SchoolTimeProvider: TimelineProvider {
    func placeholder(in context: Context) -> SchoolTimeEntry {
        SchoolTimeEntry(date: .now, subject: "수학", remaining: "25분 남음", todayRows: ["1교시 국어", "2교시 수학", "3교시 영어"])
    }

    func getSnapshot(in context: Context, completion: @escaping (SchoolTimeEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<SchoolTimeEntry>) -> Void) {
        let entry = readSharedEntry()
        let nextMinute = Calendar.current.date(byAdding: .minute, value: 1, to: .now) ?? .now.addingTimeInterval(60)
        completion(Timeline(entries: [entry], policy: .after(nextMinute)))
    }

    private func readSharedEntry() -> SchoolTimeEntry {
        let defaults = UserDefaults(suiteName: "group.com.blossom.schooltime")
        let subject = defaults?.string(forKey: "current_subject") ?? "수업 없음"
        let remaining = defaults?.string(forKey: "remaining_text") ?? "시간표 확인"
        let rows = defaults?.stringArray(forKey: "today_rows") ?? []
        return SchoolTimeEntry(date: .now, subject: subject, remaining: remaining, todayRows: rows)
    }
}

struct SchoolTimeWidgetView: View {
    let entry: SchoolTimeEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(entry.subject)
                .font(.headline.bold())
            Text(entry.remaining)
                .font(.subheadline.bold())
                .foregroundStyle(.orange)
            ForEach(entry.todayRows.prefix(5), id: \.self) { row in
                Text(row)
                    .font(.caption)
                    .lineLimit(1)
            }
            Spacer()
        }
        .padding()
        .containerBackground(.background, for: .widget)
    }
}

struct SchoolTimeWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "SchoolTimeWidget", provider: SchoolTimeProvider()) { entry in
            SchoolTimeWidgetView(entry: entry)
        }
        .configurationDisplayName("SchoolTime")
        .description("현재 교시와 오늘 시간표를 보여줍니다.")
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular])
    }
}
