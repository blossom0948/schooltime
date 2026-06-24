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
    private static final int ORANGE = Color.rgb(255, 122, 0);
    private static final int ORANGE_SOFT = Color.rgb(255, 231, 210);
    private static final int OFF_WHITE = Color.rgb(251, 250, 247);
    private static final int SURFACE = Color.rgb(255, 255, 255);
    private static final int SOFT_GRAY = Color.rgb(241, 243, 245);
    private static final int INK = Color.rgb(23, 24, 28);
    private static final int MUTED = Color.rgb(92, 99, 112);
    private static final int LINE = Color.rgb(222, 226, 230);

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
        setContentView(buildContent());
        requestNotificationPermission();
        renderSchedule();
        ScheduleNotifier.update(this);
    }

    private View buildContent() {
        FrameLayout screen = new FrameLayout(this);
        screen.setBackground(backgroundGradient());

        ScrollView page = new ScrollView(this);
        page.setFillViewport(true);
        page.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(126));
        page.addView(root, matchWrap());
        screen.addView(page, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout appBar = new LinearLayout(this);
        appBar.setGravity(Gravity.CENTER_VERTICAL);
        appBar.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(appBar, matchWrapWithMargins(0, 0, 0, dp(18)));

        TextView back = icon("‹", 32);
        appBar.addView(back, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView spacer = new TextView(this);
        appBar.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView settings = icon("⚙", 22);
        settings.setOnClickListener(v -> Toast.makeText(this, "시간표 카드를 눌러 수정하세요.", Toast.LENGTH_SHORT).show());
        appBar.addView(settings, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView title = text("Schooltime", 32, INK, Typeface.BOLD);
        root.addView(title, matchWrap());

        TextView subtitle = text("잠금화면 알림에서 다음 교시를 보고, 펼치면 오늘 시간표를 확인합니다.", 14, MUTED, Typeface.NORMAL);
        subtitle.setLineSpacing(dp(2), 1f);
        root.addView(subtitle, matchWrapWithMargins(0, dp(6), 0, dp(18)));

        LinearLayout nextCard = card(Color.argb(198, 255, 255, 255), 28, 1);
        nextCard.setElevation(dp(10));
        nextCard.setOrientation(LinearLayout.HORIZONTAL);
        nextCard.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(nextCard, matchWrapWithMargins(0, 0, 0, dp(18)));

        TextView badge = text("다음", 13, Color.WHITE, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(ORANGE, 18));
        nextCard.addView(badge, new LinearLayout.LayoutParams(dp(58), dp(38)));

        LinearLayout nextTexts = new LinearLayout(this);
        nextTexts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nextParams.leftMargin = dp(12);
        nextCard.addView(nextTexts, nextParams);

        nextTitle = text("", 20, INK, Typeface.BOLD);
        nextMeta = text("", 13, MUTED, Typeface.NORMAL);
        nextTexts.addView(nextTitle, matchWrap());
        nextTexts.addView(nextMeta, matchWrapWithMargins(0, dp(3), 0, 0));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setFillViewport(false);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        horizontal.addView(grid, wrapWrap());
        root.addView(horizontal, matchWrap());

        Button reset = liquidButton("기본값", Color.argb(180, 255, 255, 255), INK);
        reset.setOnClickListener(v -> {
            store.resetDefaults();
            renderSchedule();
            ScheduleNotifier.update(this);
        });

        Button refresh = liquidButton("알림 갱신", ORANGE, Color.WHITE);
        refresh.setOnClickListener(v -> {
            ScheduleNotifier.update(this);
            Toast.makeText(this, "잠금화면 알림을 갱신했습니다.", Toast.LENGTH_SHORT).show();
        });

        LinearLayout dock = bottomDock(reset, refresh);
        FrameLayout.LayoutParams dockParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        dockParams.setMargins(dp(18), 0, dp(18), dp(18));
        screen.addView(dock, dockParams);

        return screen;
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
        TextView empty = new TextView(this);
        row.addView(empty, new LinearLayout.LayoutParams(dp(56), dp(46)));
        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            TextView header = text(ScheduleStore.DAY_NAMES[day], 15, day == selectedDay ? Color.WHITE : INK, Typeface.BOLD);
            header.setGravity(Gravity.CENTER);
            header.setBackground(round(day == selectedDay ? ORANGE : Color.argb(170, 255, 255, 255), 18,
                    day == selectedDay ? ORANGE : Color.argb(180, 255, 255, 255), 1));
            header.setElevation(day == selectedDay ? dp(5) : dp(1));
            final int targetDay = day;
            header.setOnClickListener(v -> {
                selectedDay = targetDay;
                renderSchedule();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(74), dp(42));
            params.setMargins(dp(3), dp(2), dp(3), dp(8));
            row.addView(header, params);
        }
        return row;
    }

    private LinearLayout periodRow(int periodIndex) {
        LinearLayout row = gridRow();
        TextView label = text((periodIndex + 1) + "교시", 14, INK, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        label.setBackground(round(Color.argb(172, 255, 255, 255), 18, Color.argb(150, 255, 255, 255), 1));
        label.setElevation(dp(1));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dp(56), dp(64));
        labelParams.setMargins(0, dp(3), dp(3), dp(3));
        row.addView(label, labelParams);

        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            Period period = store.getPeriod(day, periodIndex);
            TextView cell = text(period.subject.isEmpty() ? "+" : period.subject, 14, INK, Typeface.BOLD);
            cell.setGravity(Gravity.CENTER);
            cell.setMaxLines(2);
            cell.setBackground(cellBackground(day, periodIndex));
            cell.setElevation(day == selectedDay ? dp(5) : dp(2));
            final Period target = period;
            cell.setOnClickListener(v -> {
                selectedDay = target.day;
                showEditor(target);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(74), dp(64));
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            row.addView(cell, params);
        }
        return row;
    }

    private GradientDrawable cellBackground(int day, int periodIndex) {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        boolean isNext = next != null && next.day == day && next.index == periodIndex;
        if (isNext) {
            return round(Color.rgb(255, 229, 204), 20, ORANGE, 2);
        }
        if (day == selectedDay) {
            return round(Color.argb(208, 255, 245, 235), 20, Color.rgb(255, 190, 135), 1);
        }
        return round(Color.argb(188, 255, 255, 255), 20, Color.argb(150, 255, 255, 255), 1);
    }

    private void showEditor(Period period) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(14), dp(22), dp(22));
        sheet.setBackground(bottomSheetBackground());
        sheet.setElevation(dp(16));

        TextView handle = new TextView(this);
        handle.setBackground(round(Color.rgb(218, 222, 228), 6));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(46), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        TextView title = text("정보 수정", 24, INK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title, matchWrapWithMargins(0, 0, 0, dp(20)));

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

        Button save = liquidButton("저장하기", ORANGE, Color.WHITE);
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
            Toast.makeText(this, "시간표를 저장했습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchWrapWithMargins(0, 0, 0, dp(10)));

        Button cancel = liquidButton("취소", Color.argb(190, 241, 243, 245), MUTED);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchWrap());

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.18f);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        dialog.setOnShowListener(d -> {
            Window shown = dialog.getWindow();
            if (shown != null) {
                shown.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                shown.setGravity(Gravity.BOTTOM);
                shown.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    private void renderSummary() {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        if (next == null) {
            nextTitle.setText("다음 교시 없음");
            nextMeta.setText("시간표 카드를 눌러 수업을 등록하세요.");
            return;
        }
        nextTitle.setText(next.subject.isEmpty() ? "빈 수업" : next.subject);
        String meta = ScheduleStore.DAY_NAMES[next.day] + " " + (next.index + 1) + "교시 · " + next.timeText();
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

    private LinearLayout bottomDock(Button reset, Button refresh) {
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(dp(10), dp(10), dp(10), dp(10));
        dock.setBackground(round(Color.argb(138, 255, 255, 255), 34, Color.argb(180, 255, 255, 255), 1));
        dock.setElevation(dp(18));

        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0, dp(58), 0.86f);
        resetParams.rightMargin = dp(10);
        dock.addView(reset, resetParams);

        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(0, dp(58), 1.14f);
        dock.addView(refresh, refreshParams);
        return dock;
    }

    private LinearLayout card(int color, int radius, int strokeWidth) {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));
        layout.setBackground(round(color, radius, strokeWidth > 0 ? LINE : color, strokeWidth));
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

    private TextView icon(String value, int sp) {
        TextView view = text(value, sp, INK, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setBackground(round(Color.argb(156, 255, 255, 255), 18, Color.argb(190, 255, 255, 255), 1));
        view.setElevation(dp(6));
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
        editText.setPadding(dp(16), 0, dp(16), 0);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setBackground(round(Color.argb(184, 250, 251, 253), 16, Color.argb(180, 204, 210, 218), 1));
        return editText;
    }

    private Button liquidButton(String label, int background, int textColor) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(textColor);
        int stroke = background == ORANGE ? Color.argb(170, 255, 205, 150) : Color.argb(190, 255, 255, 255);
        button.setBackground(round(background, 26, stroke, 1));
        button.setMinHeight(dp(54));
        button.setElevation(background == ORANGE ? dp(10) : dp(4));
        return button;
    }

    private GradientDrawable backgroundGradient() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.rgb(255, 252, 244),
                        OFF_WHITE,
                        Color.rgb(238, 247, 255)
                }
        );
        return drawable;
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

    private GradientDrawable bottomSheetBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(SURFACE);
        float r = dp(28);
        drawable.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return drawable;
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
