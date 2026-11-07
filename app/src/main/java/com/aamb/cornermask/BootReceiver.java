package com.aamb.cornermask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    private static final String PREFS = "aamb_settings";
    private static final String KEY_START_ON_BOOT = "start_on_boot";
    private static final String KEY_RADIUS = "corner_radius";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        android.content.SharedPreferences prefs =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_START_ON_BOOT, false)) return;
        if (!Settings.canDrawOverlays(context)) return;

        Intent service = new Intent(context, CornerOverlayService.class);
        service.putExtra(KEY_RADIUS, prefs.getFloat(KEY_RADIUS, 18f));
        service.putExtra("edge_opacity", prefs.getInt("edge_opacity", 100));
        service.putExtra("edge_darkness", prefs.getInt("edge_darkness", 100));
        service.putExtra("edge_brightness", prefs.getInt("edge_brightness", 50));
        service.putExtra("edge_contrast", prefs.getInt("edge_contrast", 100));
        service.putExtra("corner_top_left", prefs.getBoolean("corner_top_left", true));
        service.putExtra("corner_top_right", prefs.getBoolean("corner_top_right", true));
        service.putExtra("corner_bottom_right", prefs.getBoolean("corner_bottom_right", true));
        service.putExtra("corner_bottom_left", prefs.getBoolean("corner_bottom_left", true));
        try {
            context.startService(service);
        } catch (IllegalStateException ignored) {
            // Android may restrict background service starts on some versions.
        }
    }
}
