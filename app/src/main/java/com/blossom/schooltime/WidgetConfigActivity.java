package com.blossom.schooltime;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class WidgetConfigActivity extends Activity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        WidgetPrefs prefs = WidgetPrefs.load(this, appWidgetId);
        setContentView(buildContent(prefs));
    }

    private ScrollView buildContent(WidgetPrefs prefs) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(252, 249, 248));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(root);

        TextView title = label("위젯 설정", 28, Color.rgb(25, 25, 25), Typeface.BOLD);
        root.addView(title);
        root.addView(label("위젯마다 표시 방식을 따로 정할 수 있습니다.", 14, Color.rgb(105, 94, 84), Typeface.NORMAL));

        RadioGroup modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.VERTICAL);
        modeGroup.setPadding(0, dp(18), 0, dp(10));
        RadioButton next = radio("다음 교시: 과학");
        next.setId(WidgetPrefs.MODE_NEXT);
        RadioButton subjectOnly = radio("과학");
        subjectOnly.setId(WidgetPrefs.MODE_SUBJECT_ONLY);
        RadioButton periodSubject = radio("1교시: 과학");
        periodSubject.setId(WidgetPrefs.MODE_PERIOD_SUBJECT);
        modeGroup.addView(next);
        modeGroup.addView(subjectOnly);
        modeGroup.addView(periodSubject);
        modeGroup.check(prefs.mode);
        root.addView(modeGroup);

        CheckBox dark = check("다크 모드", prefs.dark);
        CheckBox weekday = check("요일 표시", prefs.showWeekday);
        CheckBox period = check("교시 표시", prefs.showPeriod);
        CheckBox time = check("시간 표시", prefs.showTime);
        root.addView(dark);
        root.addView(weekday);
        root.addView(period);
        root.addView(time);

        Button save = new Button(this);
        save.setText("위젯 적용");
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        save.setTextSize(16);
        save.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        save.setBackgroundColor(Color.rgb(255, 140, 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        params.topMargin = dp(18);
        root.addView(save, params);

        save.setOnClickListener(v -> {
            WidgetPrefs.save(this, appWidgetId, dark.isChecked(), modeGroup.getCheckedRadioButtonId(), weekday.isChecked(), period.isChecked(), time.isChecked());
            TimetableWidgetProvider.updateWidgetById(this, appWidgetId);
            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, result);
            Toast.makeText(this, "위젯 설정을 저장했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
        return scroll;
    }

    private TextView label(String value, int sp, int color, int typeface) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, typeface);
        return view;
    }

    private RadioButton radio(String value) {
        RadioButton button = new RadioButton(this);
        button.setText(value);
        button.setTextSize(16);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setMinHeight(dp(46));
        return button;
    }

    private CheckBox check(String value, boolean checked) {
        CheckBox box = new CheckBox(this);
        box.setText(value);
        box.setTextSize(16);
        box.setChecked(checked);
        box.setMinHeight(dp(44));
        return box;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
