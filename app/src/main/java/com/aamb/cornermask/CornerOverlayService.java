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
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        );
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
        private float radius;

        CornerView(Context context) {
            super(context);
            radius = 18f * getResources().getDisplayMetrics().density;
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
            Path outside = new Path();
            outside.setFillType(Path.FillType.EVEN_ODD);
            outside.addRect(0f, 0f, w, h, Path.Direction.CW);
            outside.addRoundRect(new RectF(0f, 0f, w, h),
                radius, radius, Path.Direction.CCW);
            canvas.drawPath(outside, paint);
        }
    }
}
