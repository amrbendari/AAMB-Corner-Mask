package com.aamb.cornermask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("AAMB Corner Mask");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Corner Mask is ready");
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 30, 0, 30);

        Button enable = new Button(this);
        enable.setText("Enable Corner Mask");
        enable.setOnClickListener(v -> enableMask());

        Button disable = new Button(this);
        disable.setText("Disable Corner Mask");
        disable.setOnClickListener(v -> {
            stopService(new Intent(this, CornerOverlayService.class));
            status.setText("Corner Mask is disabled");
        });

        layout.addView(title);
        layout.addView(status);
        layout.addView(enable);
        layout.addView(disable);

        setContentView(layout);
    }

    private void enableMask() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
            status.setText("Allow Display over other apps");
            return;
        }

        startService(new Intent(this, CornerOverlayService.class));
        status.setText("Corner Mask is enabled");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Settings.canDrawOverlays(this)) {
            status.setText("Overlay permission granted");
        }
    }
}
