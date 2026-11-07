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

        windowManager = (WindowManager) getSystemService(
            Context.WINDOW_SERVICE
        );

        cornerView = new CornerView(this);

        WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            );

        params.gravity = Gravity.TOP | Gravity.START;

        windowManager.addView(cornerView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cornerView != null && windowManager != null) {
            windowManager.removeView(cornerView);
            cornerView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class CornerView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final float radius;

        public CornerView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            radius = 20 * getResources().getDisplayMetrics().density;
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            path.reset();

            RectF topLeft = new RectF(
                -radius, -radius, radius, radius
            );
            RectF topRight = new RectF(
                w - radius, -radius, w + radius, radius
            );
            RectF bottomLeft = new RectF(
                -radius, h - radius, radius, h + radius
            );
            RectF bottomRight = new RectF(
                w - radius, h - radius, w + radius, h + radius
            );

            path.addArc(topLeft, 0, 360);
            path.addArc(topRight, 0, 360);
            path.addArc(bottomLeft, 0, 360);
            path.addArc(bottomRight, 0, 360);

            canvas.drawPath(path, paint);
        }
    }
}
