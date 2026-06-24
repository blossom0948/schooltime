package com.blossom.schooltime;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final int TAB_TIMETABLE = 1;
    private static final int TAB_SETTINGS = 3;

    private static final int ORANGE = Color.rgb(255, 140, 0);

    private int bg;
    private int cardColor;
    private int orangeDark;
    private int orangeSoft;
    private int gray;
    private int grayDark;
    private int ink;
    private int line;

    private ScheduleStore store;
    private SharedPreferences settings;
    private LinearLayout content;
    private LinearLayout nav;
    private TextView nextTitle;
    private TextView nextMeta;
    private int selectedDay;
    private int activeTab = TAB_TIMETABLE;
    private boolean darkMode;
    private String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("school_time_settings", MODE_PRIVATE);
        darkMode = settings.getBoolean("dark_mode", false);
        accountName = settings.getString("account_name", "");
        applyPalette();
        getWindow().setStatusBarColor(ORANGE);
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        store = new ScheduleStore(this);
        selectedDay = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
        setContentView(buildScreen());
        requestNotificationPermission();
        render();
        if (canPostNotifications()) {
            SystemUiBridge.start(this);
        }
        TimetableWidgetProvider.updateAll(this);
    }

    private View buildScreen() {
        FrameLayout screen = new FrameLayout(this);
        screen.setBackgroundColor(bg);

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
        if (activeTab == TAB_SETTINGS) {
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
        content.addView(quickActions(), matchWrapWithMargins(0, dp(14), 0, 0));
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
            TextView dayText = text(ScheduleStore.DAY_NAMES[day], 13, day == selectedDay ? orangeDark : grayDark, Typeface.BOLD);
            dayText.setGravity(Gravity.CENTER);
            dayText.setBackground(day == selectedDay ? round(orangeSoft, 14) : null);
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
        TextView time = text(Period.formatMinutes(store.getPeriod(0, periodIndex).startMinutes), 12, grayDark, Typeface.NORMAL);
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
        TextView cell = text(period.subject.isEmpty() ? "" : period.subject, 11, isNextPeriod(period) ? Color.WHITE : ink, Typeface.BOLD);
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
        if (isNextPeriod(period)) {
            return round(ORANGE, 11, ORANGE, 1);
        }
        if (period.day == selectedDay) {
            return round(orangeSoft, 11, line, 1);
        }
        return round(gray, 11, line, 1);
    }

    private LinearLayout sectionHeader(String eyebrow, String title, String hint) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(text(eyebrow, 12, orangeDark, Typeface.BOLD), matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        wrap.addView(row, matchWrapWithMargins(0, dp(2), 0, 0));

        row.addView(text(title, 30, ink, Typeface.BOLD), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView chip = text(hint, 12, orangeDark, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(round(orangeSoft, 16, line, 1));
        row.addView(chip, new LinearLayout.LayoutParams(dp(105), dp(34)));
        return wrap;
    }

    private LinearLayout nextClassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(cardColor, 22, line, 1));
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

        nextTitle = text("", 19, ink, Typeface.BOLD);
        nextMeta = text("", 13, grayDark, Typeface.NORMAL);
        texts.addView(nextTitle, matchWrap());
        texts.addView(nextMeta, matchWrapWithMargins(0, dp(2), 0, 0));
        renderSummary();
        return card;
    }

    private LinearLayout quickActions() {
        LinearLayout card = baseCard();
        card.addView(text("빠른 기능", 17, ink, Typeface.BOLD), matchWrap());
        card.addView(text("자주 쓰는 기능만 아래에 모았습니다.", 13, grayDark, Typeface.NORMAL), matchWrapWithMargins(0, dp(4), 0, dp(12)));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(row1, matchWrapWithMargins(0, 0, 0, dp(8)));

        Button share = actionButton("오늘 공유", ORANGE, Color.WHITE);
        share.setOnClickListener(v -> shareTodaySchedule());
        row1.addView(share, new LinearLayout.LayoutParams(0, dp(50), 1f));

        Button memo = actionButton("준비물 메모", gray, ink);
        memo.setOnClickListener(v -> showMemoEditor());
        LinearLayout.LayoutParams memoParams = new LinearLayout.LayoutParams(0, dp(50), 1f);
        memoParams.leftMargin = dp(8);
        row1.addView(memo, memoParams);

        Button refresh = actionButton("잠금화면 알림 갱신", gray, ink);
        refresh.setOnClickListener(v -> {
            ScheduleNotifier.update(this);
            TimetableWidgetProvider.updateAll(this);
            Toast.makeText(this, "알림을 갱신했습니다.", Toast.LENGTH_SHORT).show();
        });
        card.addView(refresh, matchWrap());
        return card;
    }

    private void renderSettings() {
        content.addView(sectionHeader("SETTINGS", "설정", "앱 정보"), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(accountCard(), matchWrapWithMargins(0, 0, 0, dp(12)));
        content.addView(settingActionCard("다크 모드", darkMode ? "켜짐" : "꺼짐", darkMode ? "라이트 모드로 변경" : "다크 모드로 변경", this::toggleDarkMode), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(settingActionCard("시간표 초기화", "기본 과목과 시간으로 되돌립니다.", "초기화", this::resetSchedule), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(infoCard("앱 버전", "SchoolTime 1.6"), matchWrapWithMargins(0, 0, 0, dp(10)));
        content.addView(infoCard("데이터", "계정과 시간표는 현재 이 기기 안에 저장됩니다."), matchWrap());
    }

    private LinearLayout accountCard() {
        LinearLayout card = baseCard();
        TextView title = text("계정", 18, ink, Typeface.BOLD);
        card.addView(title, matchWrap());
        String accountText = accountName.isEmpty()
                ? "로그인 또는 회원가입을 하면 이 기기에 계정 이름이 저장됩니다."
                : accountName + " 계정으로 로그인됨";
        card.addView(text(accountText, 13, grayDark, Typeface.NORMAL), matchWrapWithMargins(0, dp(4), 0, dp(12)));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(buttons, matchWrap());

        Button login = actionButton("로그인", ORANGE, Color.WHITE);
        login.setOnClickListener(v -> showAccountDialog("로그인"));
        Button signup = actionButton(accountName.isEmpty() ? "회원가입" : "로그아웃", gray, ink);
        signup.setOnClickListener(v -> {
            if (accountName.isEmpty()) {
                showAccountDialog("회원가입");
            } else {
                accountName = "";
                settings.edit().remove("account_name").apply();
                render();
            }
        });
        buttons.addView(login, new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams signupParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        signupParams.leftMargin = dp(8);
        buttons.addView(signup, signupParams);
        return card;
    }

    private LinearLayout infoCard(String title, String body) {
        LinearLayout card = baseCard();
        card.addView(text(title, 17, ink, Typeface.BOLD), matchWrap());
        TextView bodyView = text(body, 13, grayDark, Typeface.NORMAL);
        bodyView.setLineSpacing(dp(2), 1f);
        card.addView(bodyView, matchWrapWithMargins(0, dp(5), 0, 0));
        return card;
    }

    private LinearLayout settingActionCard(String title, String body, String action, Runnable runnable) {
        LinearLayout card = baseCard();
        card.addView(text(title, 17, ink, Typeface.BOLD), matchWrap());
        card.addView(text(body, 13, grayDark, Typeface.NORMAL), matchWrapWithMargins(0, dp(5), 0, dp(12)));
        Button button = actionButton(action, gray, ink);
        button.setOnClickListener(v -> runnable.run());
        card.addView(button, matchWrap());
        return card;
    }

    private LinearLayout baseCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(round(cardColor, 18, line, 1));
        card.setElevation(dp(3));
        return card;
    }

    private void showEditor(Period period) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(16), dp(22), dp(22));
        sheet.setBackground(bottomSheetBackground());

        TextView handle = new TextView(this);
        handle.setBackground(round(Color.rgb(210, 199, 190), 999));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(48), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        TextView title = text("수업 정보 수정", 24, ink, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title, matchWrapWithMargins(0, 0, 0, dp(18)));

        EditText subject = input("과목명", period.subject);
        sheet.addView(subject, matchHeightWithMargins(dp(66), 0, 0, 0, dp(12)));

        EditText room = input("교실/메모", period.room);
        sheet.addView(room, matchHeightWithMargins(dp(66), 0, 0, 0, dp(12)));

        LinearLayout times = new LinearLayout(this);
        times.setOrientation(LinearLayout.HORIZONTAL);
        sheet.addView(times, matchWrapWithMargins(0, 0, 0, dp(14)));

        EditText start = input("시작", Period.formatMinutes(period.startMinutes));
        times.addView(start, new LinearLayout.LayoutParams(0, dp(62), 1f));

        EditText end = input("종료", Period.formatMinutes(period.endMinutes));
        LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(0, dp(62), 1f);
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
            TimetableWidgetProvider.updateAll(this);
            Toast.makeText(this, "저장했습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchHeightWithMargins(dp(58), 0, 0, 0, dp(10)));

        Button cancel = actionButton("취소", gray, grayDark);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchHeightWithMargins(dp(56), 0, 0, 0, 0));

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
        bar.setBackground(topRound(cardColor, 22));
        bar.setElevation(dp(14));
        return bar;
    }

    private void updateNav() {
        nav.removeAllViews();
        nav.addView(navItem("시간표", TAB_TIMETABLE), navParams());
        nav.addView(navItem("설정", TAB_SETTINGS), navParams());
    }

    private TextView navItem(String label, int tab) {
        boolean active = activeTab == tab;
        TextView item = text(label, 13, active ? Color.rgb(45, 24, 0) : grayDark, Typeface.BOLD);
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

    private boolean isNextPeriod(Period period) {
        Period next = store.findCurrentOrNext(Calendar.getInstance());
        return next != null && next.day == period.day && next.index == period.index;
    }

    private void applyPalette() {
        if (darkMode) {
            bg = Color.rgb(22, 21, 20);
            cardColor = Color.rgb(32, 30, 29);
            orangeDark = Color.rgb(255, 183, 125);
            orangeSoft = Color.rgb(54, 39, 27);
            gray = Color.rgb(42, 40, 38);
            grayDark = Color.rgb(210, 199, 190);
            ink = Color.rgb(246, 241, 237);
            line = Color.rgb(79, 66, 55);
        } else {
            bg = Color.rgb(252, 249, 248);
            cardColor = Color.WHITE;
            orangeDark = Color.rgb(128, 68, 0);
            orangeSoft = Color.rgb(255, 242, 229);
            gray = Color.rgb(246, 244, 242);
            grayDark = Color.rgb(105, 94, 84);
            ink = Color.rgb(25, 25, 25);
            line = Color.rgb(229, 221, 215);
        }
    }

    private void rebuildScreen() {
        applyPalette();
        getWindow().setStatusBarColor(ORANGE);
        setContentView(buildScreen());
        render();
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        settings.edit().putBoolean("dark_mode", darkMode).apply();
        rebuildScreen();
    }

    private void resetSchedule() {
        store.resetDefaults();
        activeTab = TAB_TIMETABLE;
        render();
        ScheduleNotifier.update(this);
        TimetableWidgetProvider.updateAll(this);
        Toast.makeText(this, "기본 시간표로 초기화했습니다.", Toast.LENGTH_SHORT).show();
    }

    private void shareTodaySchedule() {
        int day = Math.max(0, store.getTodaySchoolDay(Calendar.getInstance()));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "오늘 시간표");
        intent.putExtra(Intent.EXTRA_TEXT, "오늘 시간표\n\n" + store.formatDayLine(day));
        startActivity(Intent.createChooser(intent, "시간표 공유"));
    }

    private void showAccountDialog(String mode) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        LinearLayout sheet = editorSheet();
        TextView title = text(mode, 24, ink, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title, matchWrapWithMargins(0, 0, 0, dp(18)));

        EditText name = input("이름 또는 이메일", accountName);
        sheet.addView(name, matchHeightWithMargins(dp(66), 0, 0, 0, dp(12)));

        EditText password = input("비밀번호", "");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        sheet.addView(password, matchHeightWithMargins(dp(66), 0, 0, 0, dp(14)));

        Button save = actionButton(mode + " 완료", ORANGE, Color.WHITE);
        save.setOnClickListener(v -> {
            String value = name.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "계정 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            accountName = value;
            settings.edit().putString("account_name", accountName).apply();
            dialog.dismiss();
            render();
            Toast.makeText(this, mode + "되었습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchHeightWithMargins(dp(58), 0, 0, 0, dp(10)));

        Button cancel = actionButton("닫기", gray, grayDark);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchHeightWithMargins(dp(56), 0, 0, 0, 0));

        showBottomDialog(dialog, sheet);
    }

    private void showMemoEditor() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        LinearLayout sheet = editorSheet();
        TextView title = text("준비물 메모", 24, ink, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title, matchWrapWithMargins(0, 0, 0, dp(18)));

        EditText memo = input("예: 체육복, 수행평가 자료", settings.getString("memo", ""));
        memo.setSingleLine(false);
        memo.setGravity(Gravity.TOP | Gravity.LEFT);
        sheet.addView(memo, matchHeightWithMargins(dp(120), 0, 0, 0, dp(14)));

        Button save = actionButton("메모 저장", ORANGE, Color.WHITE);
        save.setOnClickListener(v -> {
            settings.edit().putString("memo", memo.getText().toString()).apply();
            dialog.dismiss();
            Toast.makeText(this, "메모를 저장했습니다.", Toast.LENGTH_SHORT).show();
        });
        sheet.addView(save, matchHeightWithMargins(dp(58), 0, 0, 0, dp(10)));

        Button cancel = actionButton("닫기", gray, grayDark);
        cancel.setOnClickListener(v -> dialog.dismiss());
        sheet.addView(cancel, matchHeightWithMargins(dp(56), 0, 0, 0, 0));

        showBottomDialog(dialog, sheet);
    }

    private LinearLayout editorSheet() {
        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(16), dp(22), dp(22));
        sheet.setBackground(bottomSheetBackground());

        TextView handle = new TextView(this);
        handle.setBackground(round(darkMode ? Color.rgb(90, 82, 76) : Color.rgb(210, 199, 190), 999));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(48), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);
        return sheet;
    }

    private void showBottomDialog(Dialog dialog, View view) {
        dialog.setContentView(view);
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
            SystemUiBridge.start(this);
            TimetableWidgetProvider.updateAll(this);
        }
    }

    private boolean canPostNotifications() {
        return Build.VERSION.SDK_INT < 33
                || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
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
        editText.setTextColor(ink);
        editText.setHintTextColor(grayDark);
        editText.setPadding(dp(15), 0, dp(15), 0);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setBackground(round(darkMode ? Color.rgb(44, 42, 40) : Color.rgb(250, 248, 246), 14, line, 1));
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
        drawable.setColor(cardColor);
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

    private LinearLayout.LayoutParams matchHeightWithMargins(int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        );
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
