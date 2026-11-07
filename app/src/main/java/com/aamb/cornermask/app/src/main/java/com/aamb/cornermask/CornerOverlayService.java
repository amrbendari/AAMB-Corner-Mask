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

    private WindowManager wm;
    private CornerView view;

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = new CornerView(this);

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        );

        p.gravity = Gravity.TOP | Gravity.START;
        wm.addView(view, p);
    }

    @Override
    public void onDestroy() {
        if (view != null) {
            wm.removeView(view);
            view = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class CornerView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final float radius;

        CornerView(Context context) {
            super(context);
            radius = 20 * getResources().getDisplayMetrics().density;
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            path.reset();

            path.addRoundRect(
                new RectF(0, 0, w, h),
                radius,
                radius,
                Path.Direction.CW
            );

            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawPath(path, paint);
        }
    }
}
