package com.blossom.schooltime;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(45, 108, 223);
    private static final int INK = Color.rgb(22, 27, 34);
    private static final int MUTED = Color.rgb(95, 104, 119);
    private static final int LINE = Color.rgb(223, 228, 235);
    private static final int SURFACE = Color.rgb(247, 249, 252);

    private ScheduleStore store;
    private LinearLayout dayTabs;
    private LinearLayout rows;
    private TextView nextTitle;
    private TextView nextMeta;
    private int selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new ScheduleStore(this);
        selectedDay = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
        setContentView(buildContent());
        requestNotificationPermission();
        renderDay();
        ScheduleNotifier.update(this);
    }

    private View buildContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        scrollView.addView(root, matchWrap());

        TextView appName = text("SchoolTime", 28, INK, Typeface.BOLD);
        root.addView(appName, matchWrap());

        TextView subtitle = text("잠금화면 알림에서 다음 교시를 보고, 펼치면 오늘 시간표를 확인합니다.", 14, MUTED, Typeface.NORMAL);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        root.addView(subtitle, matchWrap());

        LinearLayout nowCard = panel();
        nextTitle = text("", 24, Color.WHITE, Typeface.BOLD);
        nextMeta = text("", 14, Color.argb(220, 255, 255, 255), Typeface.NORMAL);
        nowCard.setBackgroundColor(BLUE);
        nowCard.addView(nextTitle, matchWrap());
        nowCard.addView(nextMeta, matchWrap());
        root.addView(nowCard, matchWrapWithMargins(0, 0, 0, dp(14)));

        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        dayTabs = new LinearLayout(this);
        dayTabs.setOrientation(LinearLayout.HORIZONTAL);
        tabScroll.addView(dayTabs, matchWrap());
        root.addView(tabScroll, matchWrapWithMargins(0, 0, 0, dp(12)));

        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        root.addView(rows, matchWrap());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(16), 0, 0);
        root.addView(actions, matchWrap());

        Button save = button("저장하고 알림 갱신");
        save.setOnClickListener(v -> {
            saveRows();
            ScheduleNotifier.update(this);
            Toast.makeText(this, "시간표와 잠금화면 알림을 갱신했습니다.", Toast.LENGTH_SHORT).show();
        });
        actions.addView(save, weightedButton());

        Button reset = button("기본값");
        reset.setOnClickListener(v -> {
            store.resetDefaults();
            renderDay();
            ScheduleNotifier.update(this);
        });
        LinearLayout.LayoutParams resetParams = weightedButton();
        resetParams.leftMargin = dp(10);
        actions.addView(reset, resetParams);

        return scrollView;
    }

    private void renderDay() {
        renderTabs();
        renderSummary();
        rows.removeAllViews();
        List<Period> periods = store.getDay(selectedDay);
        for (Period period : periods) {
            rows.addView(periodRow(period), matchWrapWithMargins(0, 0, 0, dp(10)));
        }
    }

    private void renderTabs() {
        dayTabs.removeAllViews();
        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            Button tab = button(ScheduleStore.DAY_NAMES[day]);
            tab.setTextColor(day == selectedDay ? Color.WHITE : INK);
            tab.setBackgroundColor(day == selectedDay ? BLUE : SURFACE);
            final int targetDay = day;
            tab.setOnClickListener(v -> {
                saveRows();
                selectedDay = targetDay;
                renderDay();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(66), dp(42));
            params.rightMargin = dp(8);
            dayTabs.addView(tab, params);
        }
    }

    private void renderSummary() {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        if (next == null) {
            nextTitle.setText("다음 교시 없음");
            nextMeta.setText("시간표를 먼저 등록하세요.");
            return;
        }
        nextTitle.setText(next.subject);
        String meta = ScheduleStore.DAY_NAMES[next.day] + " " + (next.index + 1) + "교시 · " + next.timeText();
        if (!next.room.trim().isEmpty()) {
            meta += " · " + next.room;
        }
        nextMeta.setText(meta);
    }

    private View periodRow(Period period) {
        LinearLayout row = panel();
        row.setTag(period.index);

        TextView label = text((period.index + 1) + "교시", 16, INK, Typeface.BOLD);
        row.addView(label, matchWrap());

        EditText subject = input("과목", period.subject);
        subject.setTag("subject");
        row.addView(subject, matchWrapWithMargins(0, dp(8), 0, 0));

        EditText room = input("교실/메모", period.room);
        room.setTag("room");
        row.addView(room, matchWrapWithMargins(0, dp(8), 0, 0));

        LinearLayout times = new LinearLayout(this);
        times.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(times, matchWrapWithMargins(0, dp(8), 0, 0));

        EditText start = input("시작", Period.formatMinutes(period.startMinutes));
        start.setTag("start");
        times.addView(start, weightedInput());

        EditText end = input("종료", Period.formatMinutes(period.endMinutes));
        end.setTag("end");
        LinearLayout.LayoutParams endParams = weightedInput();
        endParams.leftMargin = dp(8);
        times.addView(end, endParams);

        return row;
    }

    private void saveRows() {
        for (int i = 0; i < rows.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) rows.getChildAt(i);
            int index = (int) row.getTag();
            EditText subject = findInput(row, "subject");
            EditText room = findInput(row, "room");
            EditText start = findInput(row, "start");
            EditText end = findInput(row, "end");
            Period old = store.getPeriod(selectedDay, index);
            int startMinutes = Period.parseMinutes(start.getText().toString(), old.startMinutes);
            int endMinutes = Period.parseMinutes(end.getText().toString(), old.endMinutes);
            if (endMinutes <= startMinutes) {
                endMinutes = startMinutes + 45;
            }
            store.savePeriod(new Period(
                    selectedDay,
                    index,
                    startMinutes,
                    endMinutes,
                    subject.getText().toString().trim(),
                    room.getText().toString().trim()
            ));
        }
        renderSummary();
    }

    private EditText findInput(LinearLayout row, String tag) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (tag.equals(child.getTag())) {
                return (EditText) child;
            }
            if (child instanceof LinearLayout) {
                LinearLayout group = (LinearLayout) child;
                for (int j = 0; j < group.getChildCount(); j++) {
                    View nested = group.getChildAt(j);
                    if (tag.equals(nested.getTag())) {
                        return (EditText) nested;
                    }
                }
            }
        }
        throw new IllegalStateException("Missing input " + tag);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ScheduleNotifier.update(this);
        }
    }

    private LinearLayout panel() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(14), dp(14), dp(14));
        layout.setBackgroundColor(SURFACE);
        return layout;
    }

    private TextView text(String value, int sp, int color, int typeface) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, typeface);
        view.setIncludeFontPadding(true);
        return view;
    }

    private EditText input(String hint, String value) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(value);
        editText.setSingleLine(true);
        editText.setTextSize(16);
        editText.setTextColor(INK);
        editText.setHintTextColor(MUTED);
        editText.setBackgroundColor(Color.WHITE);
        editText.setPadding(dp(10), 0, dp(10), 0);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        return editText;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(INK);
        button.setBackgroundColor(SURFACE);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams matchWrapWithMargins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private LinearLayout.LayoutParams weightedInput() {
        return new LinearLayout.LayoutParams(0, dp(48), 1f);
    }

    private LinearLayout.LayoutParams weightedButton() {
        return new LinearLayout.LayoutParams(0, dp(48), 1f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
