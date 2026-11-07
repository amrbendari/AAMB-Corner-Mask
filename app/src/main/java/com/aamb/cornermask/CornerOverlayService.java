package com.aamb.cornermask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CornerOverlayService extends Service {
    private WindowManager windowManager;
    private CornerView cornerView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        cornerView = new CornerView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        );
        // Draw against the physical display edges, including areas normally
        // reserved for system bars and display cutouts.
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(cornerView, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("corner_radius") && cornerView != null) {
            cornerView.setRadius(intent.getFloatExtra("corner_radius", 18f));
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (cornerView != null && windowManager != null) {
            windowManager.removeView(cornerView);
            cornerView = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private static class CornerView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path outside = new Path();
        private final RectF innerBounds = new RectF();
        private float radius;

        CornerView(Context context) {
            super(context);
            radius = 18f * getResources().getDisplayMetrics().density;
            // 75% transparent black (alpha 64/255) for a softer corner mask.
            paint.setColor(Color.argb(255, 0, 0, 0));
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setDither(true);
            setWillNotDraw(false);
            setSystemUiVisibility(
                SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        void setRadius(float dp) {
            radius = dp * getResources().getDisplayMetrics().density;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0f || h <= 0f) return;

            outside.reset();
            outside.setFillType(Path.FillType.EVEN_ODD);
            outside.addRect(-1f, -1f, w + 1f, h + 1f, Path.Direction.CW);

            innerBounds.set(0f, 0f, w, h);
            float r = Math.min(radius, Math.min(w, h) / 2f);
            outside.addRoundRect(innerBounds, r, r, Path.Direction.CCW);

            canvas.drawPath(outside, paint);
        }
    }
}
