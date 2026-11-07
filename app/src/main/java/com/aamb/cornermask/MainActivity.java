package com.aamb.cornermask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "aamb_settings";
    private static final String KEY_RADIUS = "corner_radius";
    private static final String KEY_DARK = "dark_mode";

    private SharedPreferences prefs;
    private TextView status;
    private TextView radiusValue;
    private Slider radiusSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean dark = prefs.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
            dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("AAMB");
        toolbar.setSubtitle("Corner Mask");
        toolbar.setElevation(dp(2));
        root.addView(toolbar, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(28));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView intro = text("خصص شكل زوايا شاشتك", 23, true);
        content.addView(intro);
        TextView description = text("تحكم في درجة التدوير ومظهر التطبيق.", 14, false);
        description.setAlpha(0.72f);
        LinearLayout.LayoutParams descParams = params(0, 0, 0, 20);
        content.addView(description, descParams);

        MaterialCardView radiusCard = card();
        LinearLayout radiusBox = column();
        radiusBox.setPadding(dp(18), dp(16), dp(18), dp(14));
        LinearLayout radiusHeader = new LinearLayout(this);
        radiusHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView radiusTitle = text("درجة تدوير الحواف", 17, true);
        radiusHeader.addView(radiusTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        radiusValue = text("18 dp", 16, true);
        radiusHeader.addView(radiusValue);
        radiusBox.addView(radiusHeader);

        radiusSlider = new Slider(this);
        radiusSlider.setValueFrom(0f);
        radiusSlider.setValueTo(40f);
        radiusSlider.setStepSize(1f);
        radiusSlider.setValue(Math.max(0f, Math.min(40f,
            prefs.getFloat(KEY_RADIUS, 18f))));
        radiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            radiusValue.setText(((int) value) + " dp");
            if (fromUser) {
                prefs.edit().putFloat(KEY_RADIUS, value).apply();
            }
        });
        radiusSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override public void onStartTrackingTouch(Slider slider) { }
            @Override public void onStopTrackingTouch(Slider slider) {
                applyRadius(slider.getValue());
            }
        });
        radiusBox.addView(radiusSlider);
        TextView range = text("من 0 إلى 40 dp", 12, false);
        range.setAlpha(0.65f);
        radiusBox.addView(range);
        radiusCard.addView(radiusBox);
        content.addView(radiusCard, params(0, 0, 0, 16));

        MaterialCardView appearanceCard = card();
        LinearLayout appearance = new LinearLayout(this);
        appearance.setOrientation(LinearLayout.HORIZONTAL);
        appearance.setGravity(Gravity.CENTER_VERTICAL);
        appearance.setPadding(dp(18), dp(8), dp(12), dp(8));
        LinearLayout labels = column();
        labels.addView(text("الوضع الليلي", 16, true));
        TextView modeHint = text("تبديل مظهر واجهة التطبيق", 12, false);
        modeHint.setAlpha(0.7f);
        labels.addView(modeHint);
        appearance.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        SwitchMaterial darkSwitch = new SwitchMaterial(this);
        darkSwitch.setChecked(dark);
        appearance.addView(darkSwitch);
        darkSwitch.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(KEY_DARK, checked).apply();
            AppCompatDelegate.setDefaultNightMode(checked
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        });
        appearanceCard.addView(appearance);
        content.addView(appearanceCard, params(0, 0, 0, 22));

        status = text("جاهز للتشغيل", 14, false);
        status.setGravity(Gravity.CENTER);
        content.addView(status, params(0, 0, 0, 12));

        MaterialButton enable = new MaterialButton(this);
        enable.setText("تشغيل الحواف");
        enable.setIconResource(android.R.drawable.ic_media_play);
        enable.setOnClickListener(v -> enableMask());
        content.addView(enable, params(0, 0, 0, 10));

        MaterialButton disable = new MaterialButton(this);
        disable.setText("إيقاف الحواف");
        disable.setIconResource(android.R.drawable.ic_media_pause);
        disable.setOnClickListener(v -> {
            stopService(new Intent(this, CornerOverlayService.class));
            status.setText("تم إيقاف الحواف");
        });
        content.addView(disable);

        setContentView(root);
    }

    private void enableMask() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            status.setText("اسمح للتطبيق بالظهور فوق التطبيقات");
            return;
        }
        Intent service = new Intent(this, CornerOverlayService.class);
        service.putExtra(KEY_RADIUS, prefs.getFloat(KEY_RADIUS, 18f));
        startService(service);
        status.setText("الحواف مفعّلة");
    }

    private void applyRadius(float radius) {
        prefs.edit().putFloat(KEY_RADIUS, radius).apply();
        if (Settings.canDrawOverlays(this)) {
            Intent service = new Intent(this, CornerOverlayService.class);
            service.putExtra(KEY_RADIUS, radius);
            startService(service);
        }
    }

    private MaterialCardView card() {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(20));
        card.setCardElevation(dp(1));
        card.setUseCompatPadding(false);
        return card;
    }

    private LinearLayout column() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams params(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
