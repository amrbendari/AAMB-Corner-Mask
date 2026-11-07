package com.aamb.cornermask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Four small, independent corner overlays.
 *
 * Keeping each window limited to its own corner avoids covering the whole
 * display with a transparent full-screen window and makes the curved edge
 * shape explicit. The windows are non-touchable so taps pass to the app below.
 */
public class CornerOverlayService extends Service {
    private static final int CORNER_TL = 0;
    private static final int CORNER_TR = 1;
    private static final int CORNER_BR = 2;
    private static final int CORNER_BL = 3;

    // Android 12+ limits untrusted, non-touchable overlay opacity for safe
    // touch pass-through. Keep each corner window at or below this threshold.
    private static final float MAX_PASS_THROUGH_ALPHA = 0.80f;

    private WindowManager windowManager;
    private final CornerView[] cornerViews = new CornerView[4];
    private final WindowManager.LayoutParams[] windowParams =
        new WindowManager.LayoutParams[4];

    private float radiusDp = 18f;
    private int edgeOpacity = 100;
    private int edgeDarkness = 100;
    private int edgeBrightness = 50;
    private int edgeContrast = 100;
    private final boolean[] enabled = { true, true, true, true };

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        applyCorners();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            radiusDp = clamp(intent.getFloatExtra("corner_radius", radiusDp), 0f, 80f);
            edgeOpacity = clamp(intent.getIntExtra("edge_opacity", edgeOpacity), 0, 100);
            edgeDarkness = clamp(intent.getIntExtra("edge_darkness", edgeDarkness), 0, 100);
            edgeBrightness = clamp(intent.getIntExtra("edge_brightness", edgeBrightness), 0, 100);
            edgeContrast = clamp(intent.getIntExtra("edge_contrast", edgeContrast), 0, 200);

            enabled[CORNER_TL] = intent.getBooleanExtra("corner_top_left", enabled[CORNER_TL]);
            enabled[CORNER_TR] = intent.getBooleanExtra("corner_top_right", enabled[CORNER_TR]);
            enabled[CORNER_BR] = intent.getBooleanExtra("corner_bottom_right", enabled[CORNER_BR]);
            enabled[CORNER_BL] = intent.getBooleanExtra("corner_bottom_left", enabled[CORNER_BL]);
        }
        applyCorners();
        return START_STICKY;
    }

    private void applyCorners() {
        if (windowManager == null) return;

        final float density = getResources().getDisplayMetrics().density;
        final int sizePx = Math.max(1, Math.round(radiusDp * density));

        float windowAlpha = Math.min(MAX_PASS_THROUGH_ALPHA, edgeOpacity / 100f);
        float channel = 255f - edgeDarkness * 2.55f;
        channel += (edgeBrightness - 50) * 2.55f;
        channel = (channel - 128f) * (edgeContrast / 100f) + 128f;
        int gray = clamp(Math.round(channel), 0, 255);

        for (int i = 0; i < 4; i++) {
            if (!enabled[i] || radiusDp <= 0f || windowAlpha <= 0f) {
                removeCorner(i);
                continue;
            }

            CornerView view = cornerViews[i];
            if (view == null) {
                view = new CornerView(this, i);
                cornerViews[i] = view;
                WindowManager.LayoutParams params = createParams(i, sizePx, windowAlpha);
                windowParams[i] = params;
                view.setMaskColor(Color.rgb(gray, gray, gray));
                try {
                    windowManager.addView(view, params);
                } catch (RuntimeException error) {
                    cornerViews[i] = null;
                    windowParams[i] = null;
                    view = null;
                }
            } else {
                view.setMaskColor(Color.rgb(gray, gray, gray));
                WindowManager.LayoutParams params = windowParams[i];
                if (params.width != sizePx || params.height != sizePx ||
                    Math.abs(params.alpha - windowAlpha) > 0.001f) {
                    params.width = sizePx;
                    params.height = sizePx;
                    params.alpha = windowAlpha;
                    try {
                        windowManager.updateViewLayout(view, params);
                    } catch (RuntimeException ignored) { }
                }
            }
        }
    }

    private WindowManager.LayoutParams createParams(int corner, int sizePx, float alpha) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        );

        params.alpha = alpha;
        switch (corner) {
            case CORNER_TL:
                params.gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case CORNER_TR:
                params.gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case CORNER_BR:
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            default:
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
        }

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }
        params.setTitle("AAMB Corner " + corner);
        return params;
    }

    private void removeCorner(int index) {
        CornerView view = cornerViews[index];
        if (view != null && windowManager != null) {
            try {
                windowManager.removeView(view);
            } catch (RuntimeException ignored) { }
        }
        cornerViews[index] = null;
        windowParams[index] = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Gravity anchors the four small windows to the physical display
        // corners. Re-apply their dimensions after density/orientation changes.
        applyCorners();
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < cornerViews.length; i++) removeCorner(i);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class CornerView extends View {
        private final int corner;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Path wedge = new Path();
        private final RectF arcBounds = new RectF();

        CornerView(Context context, int corner) {
            super(context);
            this.corner = corner;
            setWillNotDraw(false);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        void setMaskColor(int color) {
            if (paint.getColor() != color) {
                paint.setColor(color);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0f || h <= 0f) return;

            // The arc is a true quarter-circle, not a rounded rectangle
            // covering the entire screen. Everything outside this wedge is
            // transparent, so no square black patches appear beside the curve.
            wedge.reset();
            wedge.moveTo(0f, 0f);
            wedge.lineTo(w, 0f);
            arcBounds.set(0f, 0f, 2f * w, 2f * h);
            wedge.arcTo(arcBounds, -90f, -90f, false);
            wedge.close();

            canvas.save();
            if (corner == CORNER_TR || corner == CORNER_BR) {
                canvas.scale(-1f, 1f, w / 2f, h / 2f);
            }
            if (corner == CORNER_BR || corner == CORNER_BL) {
                canvas.scale(1f, -1f, w / 2f, h / 2f);
            }
            canvas.drawPath(wedge, paint);
            canvas.restore();
        }
    }
}
