package com.blossom.schooltime;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(252, 249, 248);
    private static final int CARD = Color.WHITE;
    private static final int CONTAINER = Color.rgb(246, 243, 242);
    private static final int ORANGE = Color.rgb(255, 140, 0);
    private static final int ORANGE_DARK = Color.rgb(144, 77, 0);
    private static final int PEACH = Color.rgb(255, 220, 195);
    private static final int BLUE = Color.rgb(0, 96, 172);
    private static final int BLUE_SOFT = Color.rgb(212, 227, 255);
    private static final int INK = Color.rgb(27, 28, 28);
    private static final int MUTED = Color.rgb(86, 67, 52);
    private static final int LINE = Color.rgb(221, 193, 174);
    private static final int NEUTRAL_LINE = Color.rgb(235, 229, 224);

    private ScheduleStore store;
    private LinearLayout grid;
    private TextView nextTitle;
    private TextView nextMeta;
    private int selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new ScheduleStore(this);
        selectedDay = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
        setContentView(buildScreen());
        requestNotificationPermission();
        renderSchedule();
        ScheduleNotifier.update(this);
    }

    private View buildScreen() {
        FrameLayout screen = new FrameLayout(this);
        screen.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(104));
        screen.addView(scroll, fullFrame());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(24));
        scroll.addView(root, matchWrap());

        root.addView(topBar(), matchWrapWithMargins(0, 0, 0, dp(26)));
        root.addView(sectionHeader(), matchWrapWithMargins(0, 0, 0, dp(14)));
        root.addView(nextClassCard(), matchWrapWithMargins(0, 0, 0, dp(18)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setClipToPadding(false);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        horizontal.addView(grid, wrapWrap());
        root.addView(horizontal, matchWrap());

        root.addView(tipCard(), matchWrapWithMargins(0, dp(24), 0, 0));

        screen.addView(fab(), fabParams());
        screen.addView(bottomNav(), bottomNavParams());
        return screen;
    }

    private LinearLayout topBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = text("S", 18, ORANGE_DARK, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(Color.rgb(255, 239, 224), 999, ORANGE, 2));
        bar.addView(avatar, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView title = text("SchoolTime", 22, ORANGE_DARK, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = dp(12);
        bar.addView(title, titleParams);

        TextView today = text("오늘", 14, ORANGE_DARK, Typeface.BOLD);
        today.setGravity(Gravity.CENTER);
        today.setBackground(round(Color.rgb(255, 244, 232), 18, LINE, 1));
        bar.addView(today, new LinearLayout.LayoutParams(dp(58), dp(38)));
        return bar;
    }

    private LinearLayout sectionHeader() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = text("WEEKLY SETUP", 14, ORANGE_DARK, Typeface.BOLD);
        wrap.addView(eyebrow, matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        wrap.addView(row, matchWrapWithMargins(0, dp(4), 0, 0));

        TextView title = text("시간표", 34, INK, Typeface.BOLD);
        row.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView hint = text("칸을 눌러 수정", 13, MUTED, Typeface.BOLD);
        hint.setGravity(Gravity.CENTER);
        hint.setBackground(round(Color.rgb(255, 246, 238), 18, LINE, 1));
        row.addView(hint, new LinearLayout.LayoutParams(dp(116), dp(36)));
        return wrap;
    }

    private LinearLayout nextClassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(round(CARD, 24, NEUTRAL_LINE, 1));
        card.setElevation(dp(6));

        TextView chip = text("NEXT", 12, Color.WHITE, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(ORANGE, 14));
        card.addView(chip, new LinearLayout.LayoutParams(dp(58), dp(34)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(13);
        card.addView(texts, textParams);

        nextTitle = text("", 19, INK, Typeface.BOLD);
        nextMeta = text("", 13, MUTED, Typeface.NORMAL);
        texts.addView(nextTitle, matchWrap());
        texts.addView(nextMeta, matchWrapWithMargins(0, dp(3), 0, 0));
        return card;
    }

    private LinearLayout tipCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.TOP);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(round(PEACH, 18));
        card.setElevation(dp(3));

        TextView mark = text("!", 22, Color.WHITE, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(ORANGE, 12));
        card.addView(mark, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.leftMargin = dp(14);
        card.addView(copy, copyParams);

        copy.addView(text("디자인 원툴 모드", 17, INK, Typeface.BOLD), matchWrap());
        TextView body = text("빈칸이나 과목 카드를 누르면 바로 편집할 수 있습니다. 오렌지 테두리는 다음 수업입니다.", 14, MUTED, Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1f);
        copy.addView(body, matchWrapWithMargins(0, dp(5), 0, 0));
        return card;
    }

    private void renderSchedule() {
        renderSummary();
        grid.removeAllViews();
        grid.addView(headerRow(), wrapWrap());
        for (int period = 0; period < ScheduleStore.PERIODS; period++) {
            grid.addView(periodRow(period), wrapWrap());
        }
    }

    private LinearLayout headerRow() {
        LinearLayout row = gridRow();
        TextView blank = new TextView(this);
        row.addView(blank, new LinearLayout.LayoutParams(dp(62), dp(42)));
        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.CENTER);

            TextView dayText = text(ScheduleStore.DAY_NAMES[day], 13, day == selectedDay ? ORANGE_DARK : MUTED, Typeface.BOLD);
            dayText.setGravity(Gravity.CENTER);
            column.addView(dayText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(22)));

            TextView dot = new TextView(this);
            dot.setBackground(round(day == selectedDay ? ORANGE : LINE, 999));
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(6), dp(6));
            dotParams.topMargin = dp(2);
            column.addView(dot, dotParams);

            final int targetDay = day;
            column.setOnClickListener(v -> {
                selectedDay = targetDay;
                renderSchedule();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(70), dp(42));
            params.setMargins(dp(2), 0, dp(2), dp(8));
            row.addView(column, params);
        }
        return row;
    }

    private LinearLayout periodRow(int periodIndex) {
        LinearLayout row = gridRow();

        TextView time = text(Period.formatMinutes(store.getPeriod(0, periodIndex).startMinutes), 13, MUTED, Typeface.NORMAL);
        time.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(dp(56), dp(62));
        timeParams.setMargins(0, dp(3), dp(6), dp(3));
        row.addView(time, timeParams);

        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            Period period = store.getPeriod(day, periodIndex);
            LinearLayout cell = classCell(period);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(70), dp(62));
            params.setMargins(dp(2), dp(3), dp(2), dp(3));
            row.addView(cell, params);
        }
        return row;
    }

    private LinearLayout classCell(Period period) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(7), dp(5), dp(7), dp(5));
        cell.setBackground(cellBackground(period));
        cell.setElevation(period.day == selectedDay ? dp(3) : 0);

        TextView subject = text(period.subject.isEmpty() ? "" : shortSubject(period.subject), 11, subjectColor(period), Typeface.BOLD);
        subject.setGravity(Gravity.LEFT);
        subject.setMaxLines(1);
        cell.addView(subject, matchWrap());

        if (!period.room.trim().isEmpty()) {
            TextView room = text(period.room, 10, subjectColor(period), Typeface.NORMAL);
            room.setMaxLines(1);
            cell.addView(room, matchWrapWithMargins(0, dp(8), 0, 0));
        }

        cell.setOnClickListener(v -> {
            selectedDay = period.day;
            showEditor(period);
        });
        return cell;
    }

    private GradientDrawable cellBackground(Period period) {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        boolean isNext = next != null && next.day == period.day && next.index == period.index;
        if (isNext) {
            return round(Color.rgb(255, 245, 234), 6, ORANGE, 2);
        }
        if (period.isEmpty()) {
            return round(Color.rgb(255, 254, 253), 6, NEUTRAL_LINE, 1);
        }
        int base = subjectBase(period.subject);
        int border = subjectBorder(period.subject);
        return round(base, 6, border, 1);
    }

    private int subjectColor(Period period) {
        if (period.isEmpty()) {
            return Color.TRANSPARENT;
        }
        if (period.subject.contains("영")) return Color.rgb(180, 22, 22);
        if (period.subject.contains("수") || period.subject.contains("과")) return BLUE;
        if (period.subject.contains("체")) return Color.rgb(93, 79, 66);
        return ORANGE_DARK;
    }

    private int subjectBase(String subject) {
        if (subject.contains("영")) return Color.rgb(255, 238, 238);
        if (subject.contains("수") || subject.contains("과")) return Color.rgb(234, 244, 255);
        if (subject.contains("체")) return Color.rgb(241, 236, 231);
        return Color.rgb(255, 246, 235);
    }

    private int subjectBorder(String subject) {
        if (subject.contains("영")) return Color.rgb(219, 48, 48);
        if (subject.contains("수") || subject.contains("과")) return Color.rgb(30, 126, 220);
        if (subject.contains("체")) return Color.rgb(137, 115, 98);
        return ORANGE;
    }

    private String shortSubject(String subject) {
        String clean = subject.trim();
        if (clean.length() <= 5) {
            return clean;
        }
        return clean.substring(0, 5);
    }

    private void showEditor(Period period) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(14), dp(22), dp(22));
        sheet.setBackground(bottomSheetBackground());
        sheet.setElevation(dp(18));

        TextView handle = new TextView(this);
        handle.setBackground(round(Color.rgb(210, 199, 190), 999));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(48), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        TextView title = text("수업 정보 수정", 24, INK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title, matchWrapWithMargins(0, 0, 0, dp(18)));

        EditText subject = input("과목명", period.subject);
        sheet.addView(subject, matchWrapWithMargins(0, 0, 0, dp(12)));

        EditText room = input("교실/메모", period.room);
        sheet.addView(room, matchWrapWithMargins(0, 0, 0, dp(12)));

        LinearLayout times = new LinearLayout(this);
        times.setOrientation(LinearLayout.HORIZONTAL);
        sheet.addView(times, matchWrapWithMargins(0, 0, 0, dp(14)));

        EditText start = input("시작", Period.formatMinutes(period.startMinutes));
        times.addView(start, new LinearLayout.LayoutParams(0, dp(54), 1f));

        EditText end = input("종료", Period.formatMinutes(period.endMinutes));
        LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(0, dp(54), 1f);
        endParams.leftMargin = dp(12);
        times.addView(end, endParams);

        Button save = actionButton("저장하기", ORANGE, Color.WHITE);
        save.setOnClickListener(v -> {
            int startMinutes = Period.parseMinutes(start.getText().toString(), period.startMinutes);
            int endMinutes = Period.parseMinutes(end.getText().toString(), period.endMinutes);
            if (endMinutes <= startMinutes) {
                endMinutes = startMinutes + 45;
            }
            store.savePeriod(new Period(
                    period.day,
                    period.index,
                    startMinutes,
                    endMinutes,
                    subject.getText().toString().trim(),
                    room.getText().toString().trim()
            ));
            dialog.dismiss();
            renderSchedule();
            ScheduleNotifier.update(this);
            Toast.makeText(this, "저장했습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchWrapWithMargins(0, 0, 0, dp(10)));

        Button cancel = actionButton("취소", CONTAINER, MUTED);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchWrap());

        dialog.setContentView(sheet);
        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setDimAmount(0.24f);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setGravity(Gravity.BOTTOM);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    private Button fab() {
        Button fab = new Button(this);
        fab.setText("+");
        fab.setTextSize(30);
        fab.setTextColor(Color.rgb(65, 35, 0));
        fab.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        fab.setBackground(round(ORANGE, 999));
        fab.setElevation(dp(14));
        fab.setOnClickListener(v -> showEditor(store.getPeriod(selectedDay, 0)));
        return fab;
    }

    private LinearLayout bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(10), dp(8), dp(10), dp(8));
        nav.setBackground(topRound(CARD, 18));
        nav.setElevation(dp(18));

        nav.addView(navItem("달력", false), navParams());
        nav.addView(navItem("시간표", true), navParams());
        nav.addView(navItem("알림", false), navParams());
        nav.addView(navItem("설정", false), navParams());
        return nav;
    }

    private TextView navItem(String label, boolean active) {
        TextView item = text(label, 12, active ? Color.rgb(65, 35, 0) : MUTED, Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        item.setBackground(active ? round(ORANGE, 999) : round(Color.TRANSPARENT, 999));
        return item;
    }

    private void renderSummary() {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        if (next == null) {
            nextTitle.setText("다음 수업 없음");
            nextMeta.setText("빈칸을 눌러 시간표를 등록하세요.");
            return;
        }
        nextTitle.setText(next.subject.isEmpty() ? "빈 수업" : next.subject);
        String meta = ScheduleStore.DAY_NAMES[next.day] + "요일 " + (next.index + 1) + "교시 · " + next.timeText();
        if (!next.room.trim().isEmpty()) {
            meta += " · " + next.room;
        }
        nextMeta.setText(meta);
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

    private LinearLayout gridRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
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
        editText.setHintTextColor(Color.rgb(143, 126, 112));
        editText.setPadding(dp(16), 0, dp(16), 0);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setBackground(round(Color.rgb(250, 248, 246), 14, LINE, 1));
        return editText;
    }

    private Button actionButton(String label, int background, int textColor) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(textColor);
        button.setBackground(round(background, 18));
        button.setMinHeight(dp(56));
        button.setElevation(background == ORANGE ? dp(6) : 0);
        return button;
    }

    private GradientDrawable round(int color, int radius) {
        return round(color, radius, color, 0);
    }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable topRound(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        float r = dp(radius);
        drawable.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return drawable;
    }

    private GradientDrawable bottomSheetBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(CARD);
        float r = dp(28);
        drawable.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return drawable;
    }

    private FrameLayout.LayoutParams fullFrame() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
    }

    private FrameLayout.LayoutParams bottomNavParams() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(80),
                Gravity.BOTTOM
        );
    }

    private FrameLayout.LayoutParams fabParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.BOTTOM | Gravity.RIGHT);
        params.setMargins(0, 0, dp(20), dp(54));
        return params;
    }

    private LinearLayout.LayoutParams navParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams matchWrapWithMargins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
