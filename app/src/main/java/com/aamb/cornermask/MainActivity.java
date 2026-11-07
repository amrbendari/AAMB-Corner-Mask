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
import android.widget.PopupMenu;

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
    private static final String KEY_OPACITY = "edge_opacity";
    private static final String KEY_DARKNESS = "edge_darkness";
    private static final String KEY_BRIGHTNESS = "edge_brightness";
    private static final String KEY_CONTRAST = "edge_contrast";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LANGUAGE = "language";

    private SharedPreferences prefs;
    private TextView status;
    private TextView radiusValue;
    private Slider radiusSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String savedTheme = prefs.getString(KEY_THEME_MODE,
            prefs.getBoolean(KEY_DARK, false) ? "dark" : "light");
        applyThemeMode(savedTheme);
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("AAMB");
        toolbar.setSubtitle(tr("Corner Mask"));
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


        MaterialCardView edgeCard = card();
        LinearLayout edgeBox = column();
        edgeBox.setPadding(dp(18), dp(16), dp(18), dp(14));
        edgeBox.addView(text("مظهر الحواف", 17, true));
        Slider opacitySlider = new Slider(this);
        opacitySlider.setValueFrom(0f); opacitySlider.setValueTo(100f); opacitySlider.setStepSize(1f);
        opacitySlider.setValue(prefs.getInt(KEY_OPACITY, 100));
        TextView opacityLabel = text((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "شفافية الحواف: " : "Edge opacity: ") + prefs.getInt(KEY_OPACITY, 100) + "%", 14, false);
        edgeBox.addView(opacityLabel); edgeBox.addView(opacitySlider);
        Slider darknessSlider = new Slider(this);
        darknessSlider.setValueFrom(0f); darknessSlider.setValueTo(100f); darknessSlider.setStepSize(1f);
        darknessSlider.setValue(prefs.getInt(KEY_DARKNESS, 100));
        TextView darknessLabel = text((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "درجة السواد: " : "Black level: ") + prefs.getInt(KEY_DARKNESS, 100) + "%", 14, false);
        edgeBox.addView(darknessLabel); edgeBox.addView(darknessSlider);
        Slider brightnessSlider = new Slider(this);
        brightnessSlider.setValueFrom(0f); brightnessSlider.setValueTo(100f); brightnessSlider.setStepSize(1f);
        brightnessSlider.setValue(prefs.getInt(KEY_BRIGHTNESS, 50));
        TextView brightnessLabel = text((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "سطوع الحواف: " : "Edge brightness: ") + prefs.getInt(KEY_BRIGHTNESS, 50) + "%", 14, false);
        edgeBox.addView(brightnessLabel); edgeBox.addView(brightnessSlider);
        Slider contrastSlider = new Slider(this);
        contrastSlider.setValueFrom(0f); contrastSlider.setValueTo(200f); contrastSlider.setStepSize(1f);
        contrastSlider.setValue(prefs.getInt(KEY_CONTRAST, 100));
        TextView contrastLabel = text((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "تباين الحواف: " : "Edge contrast: ") + prefs.getInt(KEY_CONTRAST, 100) + "%", 14, false);
        edgeBox.addView(contrastLabel); edgeBox.addView(contrastSlider);
        opacitySlider.addOnChangeListener((s, value, fromUser) -> {
            int v = (int)value; opacityLabel.setText(prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "شفافية الحواف: " + v + "%" : "Edge opacity: " + v + "%");
            if (fromUser) { prefs.edit().putInt(KEY_OPACITY, v).apply(); applyEdgeStyle(); }
        });
        darknessSlider.addOnChangeListener((s, value, fromUser) -> {
            int v = (int)value; darknessLabel.setText(prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "درجة السواد: " + v + "%" : "Black level: " + v + "%");
            if (fromUser) { prefs.edit().putInt(KEY_DARKNESS, v).apply(); applyEdgeStyle(); }
        });
        brightnessSlider.addOnChangeListener((s, value, fromUser) -> {
            int v = (int)value; brightnessLabel.setText((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "سطوع الحواف: " : "Edge brightness: ") + v + "%");
            if (fromUser) { prefs.edit().putInt(KEY_BRIGHTNESS, v).apply(); applyEdgeStyle(); }
        });
        contrastSlider.addOnChangeListener((s, value, fromUser) -> {
            int v = (int)value; contrastLabel.setText((prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "تباين الحواف: " : "Edge contrast: ") + v + "%");
            if (fromUser) { prefs.edit().putInt(KEY_CONTRAST, v).apply(); applyEdgeStyle(); }
        });
        edgeCard.addView(edgeBox);
        content.addView(edgeCard, params(0, 0, 0, 16));
        MaterialCardView cornersCard = card();
        LinearLayout cornersBox = column();
        cornersBox.setPadding(dp(18), dp(16), dp(18), dp(14));
        cornersBox.addView(text("اختيار الحواف", 17, true));
        addCornerSwitch(cornersBox, "العلوي جهة اليمين", "corner_top_right");
        addCornerSwitch(cornersBox, "العلوي جهة اليسار", "corner_top_left");
        addCornerSwitch(cornersBox, "السفلي جهة اليمين", "corner_bottom_right");
        addCornerSwitch(cornersBox, "السفلي جهة اليسار", "corner_bottom_left");
        cornersCard.addView(cornersBox);
        content.addView(cornersCard, params(0, 0, 0, 16));


        MaterialCardView appearanceCard = card();
        LinearLayout appearance = new LinearLayout(this);
        appearance.setGravity(Gravity.CENTER_VERTICAL);
        appearance.setPadding(dp(18), dp(8), dp(12), dp(8));
        appearance.addView(text("مظهر التطبيق", 16, true), new LinearLayout.LayoutParams(0, -2, 1f));
        MaterialButton themeButton = new MaterialButton(this);
        themeButton.setText("");
        themeButton.setIconResource(android.R.drawable.ic_menu_manage);
        themeButton.setContentDescription(tr("اختيار مظهر التطبيق"));
        themeButton.setMinWidth(dp(52));
        appearance.addView(themeButton);
        themeButton.setOnClickListener(anchor -> {
            PopupMenu menu = new PopupMenu(this, themeButton);
            menu.getMenu().add(0, 1, 0, tr("وضع الجهاز"));
            menu.getMenu().add(0, 2, 1, tr("الوضع الفاتح"));
            menu.getMenu().add(0, 3, 2, tr("الوضع الليلي"));
            menu.setOnMenuItemClickListener(item -> {
                String mode = item.getItemId() == 1 ? "system" :
                    item.getItemId() == 2 ? "light" : "dark";
                prefs.edit().putString(KEY_THEME_MODE, mode)
                    .putBoolean(KEY_DARK, mode.equals("dark")).apply();
                applyThemeMode(mode);
                recreate();
                return true;
            });
            menu.show();
        });
        appearanceCard.addView(appearance);
        content.addView(appearanceCard, params(0, 0, 0, 22));

        MaterialCardView bootCard = card();
        LinearLayout bootRow = new LinearLayout(this);
        bootRow.setGravity(Gravity.CENTER_VERTICAL);
        bootRow.setPadding(dp(18), dp(8), dp(12), dp(8));
        LinearLayout bootLabels = column();
        bootLabels.addView(text("تشغيل التطبيق مع إعادة تشغيل الجهاز", 16, true));
        TextView bootHint = text("تفعيل الحواف تلقائيًا بعد تشغيل الجهاز", 12, false);
        bootHint.setAlpha(0.7f);
        bootLabels.addView(bootHint);
        bootRow.addView(bootLabels, new LinearLayout.LayoutParams(0, -2, 1f));
        SwitchMaterial bootSwitch = new SwitchMaterial(this);
        bootSwitch.setChecked(prefs.getBoolean("start_on_boot", false));
        bootRow.addView(bootSwitch);
        bootSwitch.setOnCheckedChangeListener((button, checked) ->
            prefs.edit().putBoolean("start_on_boot", checked).apply());
        bootCard.addView(bootRow);
        content.addView(bootCard, params(0, 0, 0, 16));

        MaterialCardView languageCard = card();
        LinearLayout languageRow = new LinearLayout(this);
        languageRow.setGravity(Gravity.CENTER_VERTICAL);
        languageRow.setPadding(dp(18), dp(8), dp(12), dp(8));
        languageRow.addView(text(tr("لغة التطبيق"), 16, true), new LinearLayout.LayoutParams(0, -2, 1f));
        MaterialButton languageButton = new MaterialButton(this);
        languageButton.setText("العربية / English");
        languageButton.setOnClickListener(v -> {
            String next = prefs.getString(KEY_LANGUAGE, "ar").equals("ar") ? "en" : "ar";
            prefs.edit().putString(KEY_LANGUAGE, next).apply();
            recreate();
        });
        languageRow.addView(languageButton);
        languageCard.addView(languageRow);
        content.addView(languageCard, params(0, 0, 0, 16));

        status = text("جاهز للتشغيل", 14, false);
        status.setGravity(Gravity.CENTER);
        content.addView(status, params(0, 0, 0, 12));

        MaterialButton enable = new MaterialButton(this);
        enable.setText(tr("تشغيل الحواف"));
        enable.setIconResource(android.R.drawable.ic_media_play);
        enable.setOnClickListener(v -> enableMask());
        content.addView(enable, params(0, 0, 0, 10));

        MaterialButton disable = new MaterialButton(this);
        disable.setText(tr("إيقاف الحواف"));
        disable.setIconResource(android.R.drawable.ic_media_pause);
        disable.setOnClickListener(v -> {
            stopService(new Intent(this, CornerOverlayService.class));
            status.setText(tr("تم إيقاف الحواف"));
        });
        content.addView(disable);

        setContentView(root);
    }

    private void enableMask() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            status.setText(tr("اسمح للتطبيق بالظهور فوق التطبيقات"));
            return;
        }
        Intent service = new Intent(this, CornerOverlayService.class);
        putAllSettings(service);
        startService(service);
        status.setText(tr("الحواف مفعّلة"));
    }

    private void applyRadius(float radius) {
        prefs.edit().putFloat(KEY_RADIUS, radius).apply();
        if (Settings.canDrawOverlays(this)) {
            Intent service = new Intent(this, CornerOverlayService.class);
            putAllSettings(service);
            service.putExtra(KEY_RADIUS, radius);
            startService(service);
        }
    }


    private void applyEdgeStyle() {
        if (Settings.canDrawOverlays(this)) {
            Intent service = new Intent(this, CornerOverlayService.class);
            putAllSettings(service);
            startService(service);
        }
    }

    private void applyThemeMode(String mode) {
        int nightMode = "system".equals(mode) ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM :
            "dark".equals(mode) ? AppCompatDelegate.MODE_NIGHT_YES :
            AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void putAllSettings(Intent service) {
        service.putExtra(KEY_RADIUS, prefs.getFloat(KEY_RADIUS, 18f));
        service.putExtra(KEY_OPACITY, prefs.getInt(KEY_OPACITY, 100));
        service.putExtra(KEY_DARKNESS, prefs.getInt(KEY_DARKNESS, 100));
        service.putExtra(KEY_BRIGHTNESS, prefs.getInt(KEY_BRIGHTNESS, 50));
        service.putExtra(KEY_CONTRAST, prefs.getInt(KEY_CONTRAST, 100));
        service.putExtra("corner_top_right", prefs.getBoolean("corner_top_right", true));
        service.putExtra("corner_top_left", prefs.getBoolean("corner_top_left", true));
        service.putExtra("corner_bottom_right", prefs.getBoolean("corner_bottom_right", true));
        service.putExtra("corner_bottom_left", prefs.getBoolean("corner_bottom_left", true));
    }

    private void addCornerSwitch(LinearLayout parent, String label, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(5), 0, dp(5));
        row.addView(text(label, 14, false), new LinearLayout.LayoutParams(0, -2, 1f));
        SwitchMaterial toggle = new SwitchMaterial(this);
        toggle.setChecked(prefs.getBoolean(key, true));
        row.addView(toggle);
        toggle.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            applyEdgeStyle();
        });
        parent.addView(row);
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

    private String tr(String value) {
        if (prefs == null || prefs.getString(KEY_LANGUAGE, "ar").equals("ar")) return value;
        switch (value) {
            case "خصص شكل زوايا شاشتك": return "Customize your screen corners";
            case "تحكم في درجة التدوير ومظهر التطبيق.": return "Adjust corner radius and appearance.";
            case "درجة تدوير الحواف": return "Corner radius";
            case "من 0 إلى 40 dp": return "From 0 to 40 dp";
            case "مظهر الحواف": return "Corner appearance";
            case "مظهر التطبيق": return "App appearance";
            case "اختيار مظهر التطبيق": return "Choose app appearance";
            case "وضع الجهاز": return "System default";
            case "الوضع الفاتح": return "Light mode";
            case "الوضع الليلي": return "Dark mode";
            case "اختيار الحواف": return "Select corners";
            case "العلوي جهة اليمين": return "Top right";
            case "العلوي جهة اليسار": return "Top left";
            case "السفلي جهة اليمين": return "Bottom right";
            case "السفلي جهة اليسار": return "Bottom left";
            case "تبديل مظهر واجهة التطبيق": return "Change the app appearance";
            case "لغة التطبيق": return "App language";
            case "تشغيل التطبيق مع إعادة تشغيل الجهاز": return "Start on device boot";
            case "تفعيل الحواف تلقائيًا بعد تشغيل الجهاز": return "Automatically enable corners after reboot";
            case "جاهز للتشغيل": return "Ready";
            case "تشغيل الحواف": return "Enable corners";
            case "إيقاف الحواف": return "Disable corners";
            case "تم إيقاف الحواف": return "Corners disabled";
            case "الحواف مفعّلة": return "Corners enabled";
            case "اسمح للتطبيق بالظهور فوق التطبيقات": return "Allow display over other apps";
            case "مستشعر الإضاءة غير متاح؛ استخدم الوضع اليدوي": return "Light sensor unavailable; use manual mode";
            default: return value;
        }
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(tr(value));
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
