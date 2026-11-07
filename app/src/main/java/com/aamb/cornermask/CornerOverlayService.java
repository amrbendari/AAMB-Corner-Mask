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
        if (intent != null && cornerView != null) {
            if (intent.hasExtra("corner_radius"))
                cornerView.setRadius(intent.getFloatExtra("corner_radius", 18f));
            cornerView.setEdgeStyle(
                intent.getIntExtra("edge_opacity", 100),
                intent.getIntExtra("edge_darkness", 100),
                intent.getIntExtra("edge_brightness", 50),
                intent.getIntExtra("edge_contrast", 100)
            );
            cornerView.setCorners(
                intent.getBooleanExtra("corner_top_left", true),
                intent.getBooleanExtra("corner_top_right", true),
                intent.getBooleanExtra("corner_bottom_right", true),
                intent.getBooleanExtra("corner_bottom_left", true)
            );
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
        private int edgeOpacity = 100;
        private int edgeDarkness = 100;
        private int edgeBrightness = 50;
        private int edgeContrast = 100;
        private boolean topRight = true, topLeft = true, bottomRight = true, bottomLeft = true;

        CornerView(Context context) {
            super(context);
            radius = 18f * getResources().getDisplayMetrics().density;
            paint.setColor(Color.BLACK);
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

        void setEdgeStyle(int opacity, int darkness, int brightness, int contrast) {
            edgeOpacity = Math.max(0, Math.min(100, opacity));
            edgeDarkness = Math.max(0, Math.min(100, darkness));
            edgeBrightness = Math.max(0, Math.min(100, brightness));
            edgeContrast = Math.max(0, Math.min(200, contrast));
            float channel = 255f - edgeDarkness * 2.55f;
            channel += (edgeBrightness - 50) * 2.55f;
            channel = (channel - 128f) * (edgeContrast / 100f) + 128f;
            int value = Math.max(0, Math.min(255, Math.round(channel)));
            int alpha = Math.round(edgeOpacity * 2.55f);
            paint.setColor(Color.argb(alpha, value, value, value));
            invalidate();
        }

        void setCorners(boolean tl, boolean tr, boolean br, boolean bl) {
            topLeft = tl; topRight = tr; bottomRight = br; bottomLeft = bl;
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
            float[] cornerRadii = new float[] {
                topLeft ? r : 0f, topLeft ? r : 0f,
                topRight ? r : 0f, topRight ? r : 0f,
                bottomRight ? r : 0f, bottomRight ? r : 0f,
                bottomLeft ? r : 0f, bottomLeft ? r : 0f
            };
            outside.addRoundRect(innerBounds, cornerRadii, Path.Direction.CCW);

            canvas.drawPath(outside, paint);
        }
    }
}
