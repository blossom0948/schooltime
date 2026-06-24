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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public final class MainActivity extends Activity {
    private static final int TAB_CALENDAR = 0;
    private static final int TAB_TIMETABLE = 1;
    private static final int TAB_ALERTS = 2;
    private static final int TAB_SETTINGS = 3;

    private static final int BG = Color.rgb(252, 249, 248);
    private static final int CARD = Color.WHITE;
    private static final int ORANGE = Color.rgb(255, 140, 0);
    private static final int ORANGE_DARK = Color.rgb(128, 68, 0);
    private static final int ORANGE_SOFT = Color.rgb(255, 242, 229);
    private static final int GRAY = Color.rgb(246, 244, 242);
    private static final int GRAY_DARK = Color.rgb(105, 94, 84);
    private static final int INK = Color.rgb(25, 25, 25);
    private static final int LINE = Color.rgb(229, 221, 215);

    private ScheduleStore store;
    private LinearLayout content;
    private LinearLayout nav;
    private TextView nextTitle;
    private TextView nextMeta;
    private int selectedDay;
    private int activeTab = TAB_TIMETABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ORANGE);
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        store = new ScheduleStore(this);
        selectedDay = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
        setContentView(buildScreen());
        requestNotificationPermission();
        render();
        ScheduleNotifier.update(this);
    }

    private View buildScreen() {
        FrameLayout screen = new FrameLayout(this);
        screen.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        screen.addView(page, fullFrame());

        page.addView(topBar(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(92) + statusBarHeight()
        ));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(88));
        page.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(18));
        scroll.addView(content, matchWrap());

        nav = bottomNav();
        screen.addView(nav, bottomNavParams());
        return screen;
    }

    private LinearLayout topBar() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setGravity(Gravity.BOTTOM);
        shell.setPadding(dp(16), statusBarHeight(), dp(16), dp(14));
        shell.setBackgroundColor(ORANGE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        shell.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        ));

        TextView icon = text("▦", 24, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(Color.argb(44, 255, 255, 255), 18, Color.argb(95, 255, 255, 255), 1));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = dp(12);
        row.addView(titles, titleParams);

        titles.addView(text("SchoolTime", 23, Color.WHITE, Typeface.BOLD), matchWrap());
        titles.addView(text("다음 교시를 잠금화면에서 바로 확인", 12, Color.rgb(255, 244, 232), Typeface.NORMAL), matchWrap());

        TextView today = text("오늘", 13, Color.WHITE, Typeface.BOLD);
        today.setGravity(Gravity.CENTER);
        today.setBackground(round(Color.argb(36, 255, 255, 255), 16, Color.argb(95, 255, 255, 255), 1));
        today.setOnClickListener(v -> {
            selectedDay = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
            activeTab = TAB_TIMETABLE;
            render();
        });
        row.addView(today, new LinearLayout.LayoutParams(dp(58), dp(36)));
        return shell;
    }

    private void render() {
        content.removeAllViews();
        if (activeTab == TAB_CALENDAR) {
            renderCalendar();
        } else if (activeTab == TAB_ALERTS) {
            renderAlerts();
        } else if (activeTab == TAB_SETTINGS) {
            renderSettings();
        } else {
            renderTimetable();
        }
        updateNav();
    }

    private void renderTimetable() {
        content.addView(sectionHeader("WEEKLY SETUP", "시간표", "칸을 눌러 수정"), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(nextClassCard(), matchWrapWithMargins(0, 0, 0, dp(14)));
        content.addView(timetableGrid(), matchWrap());
    }

    private LinearLayout timetableGrid() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(2), 0, 0);

        wrap.addView(headerRow(), matchWrap());
        for (int period = 0; period < ScheduleStore.PERIODS; period++) {
            wrap.addView(periodRow(period), matchWrap());
        }
        return wrap;
    }

    private LinearLayout headerRow() {
        LinearLayout row = gridRow();
        TextView blank = new TextView(this);
        row.addView(blank, new LinearLayout.LayoutParams(dp(43), dp(35)));

        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            TextView dayText = text(ScheduleStore.DAY_NAMES[day], 13, day == selectedDay ? ORANGE_DARK : GRAY_DARK, Typeface.BOLD);
            dayText.setGravity(Gravity.CENTER);
            dayText.setBackground(day == selectedDay ? round(ORANGE_SOFT, 14) : null);
            final int targetDay = day;
            dayText.setOnClickListener(v -> {
                selectedDay = targetDay;
                render();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(35), 1f);
            params.setMargins(dp(2), 0, dp(2), dp(4));
            row.addView(dayText, params);
        }
        return row;
    }

    private LinearLayout periodRow(int periodIndex) {
        LinearLayout row = gridRow();
        TextView time = text(Period.formatMinutes(store.getPeriod(0, periodIndex).startMinutes), 12, GRAY_DARK, Typeface.NORMAL);
        time.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(dp(43), dp(48));
        timeParams.setMargins(0, dp(2), dp(4), dp(2));
        row.addView(time, timeParams);

        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            Period period = store.getPeriod(day, periodIndex);
            TextView cell = classCell(period);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
            params.setMargins(dp(2), dp(2), dp(2), dp(2));
            row.addView(cell, params);
        }
        return row;
    }

    private TextView classCell(Period period) {
        TextView cell = text(period.subject.isEmpty() ? "" : period.subject, 11, INK, Typeface.BOLD);
        cell.setGravity(Gravity.CENTER);
        cell.setMaxLines(1);
        cell.setBackground(cellBackground(period));
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
            return round(ORANGE, 11, ORANGE, 1);
        }
        if (period.day == selectedDay) {
            return round(ORANGE_SOFT, 11, LINE, 1);
        }
        return round(GRAY, 11, LINE, 1);
    }

    private LinearLayout sectionHeader(String eyebrow, String title, String hint) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(text(eyebrow, 12, ORANGE_DARK, Typeface.BOLD), matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        wrap.addView(row, matchWrapWithMargins(0, dp(2), 0, 0));

        row.addView(text(title, 30, INK, Typeface.BOLD), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView chip = text(hint, 12, ORANGE_DARK, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(Color.rgb(255, 246, 238), 16, LINE, 1));
        row.addView(chip, new LinearLayout.LayoutParams(dp(105), dp(34)));
        return wrap;
    }

    private LinearLayout nextClassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(CARD, 22, LINE, 1));
        card.setElevation(dp(5));

        TextView chip = text("NEXT", 12, Color.WHITE, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(ORANGE, 16));
        card.addView(chip, new LinearLayout.LayoutParams(dp(62), dp(40)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(12);
        card.addView(texts, textParams);

        nextTitle = text("", 19, INK, Typeface.BOLD);
        nextMeta = text("", 13, GRAY_DARK, Typeface.NORMAL);
        texts.addView(nextTitle, matchWrap());
        texts.addView(nextMeta, matchWrapWithMargins(0, dp(2), 0, 0));
        renderSummary();
        return card;
    }

    private void renderCalendar() {
        content.addView(sectionHeader("CALENDAR", "오늘", "이번 주"), matchWrapWithMargins(0, 0, 0, dp(12)));
        Calendar now = Calendar.getInstance();
        String today = (now.get(Calendar.MONTH) + 1) + "월 " + now.get(Calendar.DAY_OF_MONTH) + "일";
        content.addView(infoCard(today, "오늘 요일은 " + dayNameFromCalendar(now) + "요일입니다."), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(infoCard("이번 주 수업", store.formatDayLine(Math.max(0, store.getTodaySchoolDay(now)))), matchWrap());
    }

    private void renderAlerts() {
        content.addView(sectionHeader("NOTIFICATIONS", "알림", "잠금화면"), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(infoCard("잠금화면 표시", "다음 교시 알림을 잠금화면에 공개로 표시합니다."), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(infoCard("알림 갱신", "시간표를 저장하면 다음 교시 알림이 자동으로 갱신됩니다."), matchWrapWithMargins(0, 0, 0, dp(14)));

        Button refresh = actionButton("알림 다시 갱신", ORANGE, Color.WHITE);
        refresh.setOnClickListener(v -> {
            ScheduleNotifier.update(this);
            Toast.makeText(this, "알림을 갱신했습니다.", Toast.LENGTH_SHORT).show();
        });
        content.addView(refresh, matchWrap());
    }

    private void renderSettings() {
        content.addView(sectionHeader("SETTINGS", "설정", "앱 정보"), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(accountCard(), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(infoCard("다크 모드", "현재 버전에서는 시스템 테마를 따릅니다."), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(infoCard("앱 버전", "SchoolTime 1.4"), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(infoCard("데이터", "시간표는 이 기기 안에 저장됩니다."), matchWrap());
    }

    private LinearLayout accountCard() {
        LinearLayout card = baseCard();
        TextView title = text("계정", 18, INK, Typeface.BOLD);
        card.addView(title, matchWrap());
        card.addView(text("로그인하면 여러 기기에서 시간표를 동기화할 수 있습니다.", 13, GRAY_DARK, Typeface.NORMAL), matchWrapWithMargins(0, dp(4), 0, dp(12)));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(buttons, matchWrap());

        Button login = actionButton("로그인", ORANGE, Color.WHITE);
        Button signup = actionButton("회원가입", GRAY, INK);
        buttons.addView(login, new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams signupParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        signupParams.leftMargin = dp(8);
        buttons.addView(signup, signupParams);
        return card;
    }

    private LinearLayout infoCard(String title, String body) {
        LinearLayout card = baseCard();
        card.addView(text(title, 17, INK, Typeface.BOLD), matchWrap());
        TextView bodyView = text(body, 13, GRAY_DARK, Typeface.NORMAL);
        bodyView.setLineSpacing(dp(2), 1f);
        card.addView(bodyView, matchWrapWithMargins(0, dp(5), 0, 0));
        return card;
    }

    private LinearLayout baseCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(round(CARD, 18, LINE, 1));
        card.setElevation(dp(3));
        return card;
    }

    private void showEditor(Period period) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(14), dp(22), dp(22));
        sheet.setBackground(bottomSheetBackground());

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
        endParams.leftMargin = dp(10);
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
            activeTab = TAB_TIMETABLE;
            render();
            ScheduleNotifier.update(this);
            Toast.makeText(this, "저장했습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchWrapWithMargins(0, 0, 0, dp(10)));

        Button cancel = actionButton("취소", GRAY, GRAY_DARK);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchWrap());

        dialog.setContentView(sheet);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.22f);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = android.R.style.Animation_InputMethod;
        }
    }

    private LinearLayout bottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(10), dp(8), dp(10), dp(8));
        bar.setBackground(topRound(CARD, 22));
        bar.setElevation(dp(14));
        return bar;
    }

    private void updateNav() {
        nav.removeAllViews();
        nav.addView(navItem("달력", TAB_CALENDAR), navParams());
        nav.addView(navItem("시간표", TAB_TIMETABLE), navParams());
        nav.addView(navItem("알림", TAB_ALERTS), navParams());
        nav.addView(navItem("설정", TAB_SETTINGS), navParams());
    }

    private TextView navItem(String label, int tab) {
        boolean active = activeTab == tab;
        TextView item = text(label, 13, active ? Color.rgb(45, 24, 0) : GRAY_DARK, Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        item.setBackground(active ? round(ORANGE, 999) : round(Color.TRANSPARENT, 999));
        item.setOnClickListener(v -> {
            activeTab = tab;
            render();
        });
        return item;
    }

    private void renderSummary() {
        if (nextTitle == null || nextMeta == null) {
            return;
        }
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

    private String dayNameFromCalendar(Calendar calendar) {
        int day = store.getTodaySchoolDay(calendar);
        if (day < 0) {
            return "주말";
        }
        return ScheduleStore.DAY_NAMES[day];
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
        editText.setHintTextColor(Color.rgb(145, 130, 118));
        editText.setPadding(dp(15), 0, dp(15), 0);
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
        button.setMinHeight(dp(54));
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
                dp(78),
                Gravity.BOTTOM
        );
    }

    private LinearLayout.LayoutParams navParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(dp(3), 0, dp(3), 0);
        return params;
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

    private int statusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return dp(24);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
