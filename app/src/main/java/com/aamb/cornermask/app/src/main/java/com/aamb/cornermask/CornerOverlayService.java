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
    private CornerView cornerView;

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        cornerView = new CornerView(this);

        WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            );

        params.gravity = Gravity.TOP | Gravity.START;
        wm.addView(cornerView, params);
    }

    @Override
    public void onDestroy() {
        if (cornerView != null) {
            wm.removeView(cornerView);
            cornerView = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class CornerView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path outside = new Path();
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

            canvas.save();

            Path rounded = new Path();
            rounded.addRoundRect(
                new RectF(0, 0, w, h),
                radius,
                radius,
                Path.Direction.CW
            );

            outside.reset();
            outside.addRect(0, 0, w, h, Path.Direction.CW);
            outside.op(rounded, Path.Op.DIFFERENCE);

            canvas.drawPath(outside, paint);
            canvas.restore();
        }
    }
}
