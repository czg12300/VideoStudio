package com.uc.mamba;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

public class Utils {
    public static void setViewShader(View view, final float elevation, final float radius, boolean b) {
        if (view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && b) {
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 0);
                }
            };
            view.setElevation(elevation);
            view.setOutlineProvider(viewOutlineProvider);
        } else {
            final Drawable background = view.getBackground();
            Drawable drawable = new Drawable() {
                private Paint mPaint;
                private Path mPath;
                private RectF rectF = new RectF();
                private Rect bgRect = new Rect();

                @Override
                public void draw(@NonNull Canvas canvas) {
                    if (mPaint == null) {
                        mPaint = new Paint();
                        mPaint.setStyle(Paint.Style.STROKE);
//                        mPaint.setColor(0xFFCFCECE);
                        mPaint.setColor(0xFFDCDCDC);
                        mPath = new Path();
                    }
                    final Rect rect = getBounds();
                    rectF.set(rect);
                    bgRect.set((int) elevation - 1, (int) elevation - 1, (int) (rect.width() - elevation + 1), (int) (rect.height() - elevation + 1));
                    background.setBounds(bgRect);
                    long last = System.currentTimeMillis();
                    final float strokeWidth = 1;
                    mPaint.setStrokeWidth(strokeWidth);
                    for (int i = 1; i < elevation; i++) {
                        float lastX = strokeWidth * i;
                        float lastY = strokeWidth * i;
                        mPaint.setAlpha((int) (i * 255 / elevation));
                        mPath.reset();
                        mPath.moveTo(lastX, lastY);
                        mPath.lineTo(rect.right - lastX, lastY);
                        mPath.lineTo(rect.right - lastX, rect.bottom - lastY);
                        mPath.lineTo(lastX, rect.bottom - lastY);
                        mPath.lineTo(lastX, lastY);
                        canvas.drawPath(mPath, mPaint);
                    }
                    background.draw(canvas);
                    long now = System.currentTimeMillis();
                    Log.d("jake", "time " + (now - last));

                }

                @Override
                public void setAlpha(int alpha) {
                    background.setAlpha(alpha);
                }

                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {
                    background.setColorFilter(colorFilter);
                }

                @Override
                public int getOpacity() {
                    return background.getOpacity();
                }
            };
            view.setBackgroundDrawable(drawable);
        }
    }


    public static void clearViewShader(View view) {
        if (view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOutlineProvider(null);
        }
    }

    public static int dpI(float value) {
        return (int) dpF(value);
    }

    public static float dpF(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, Resources.getSystem().getDisplayMetrics());
    }
}
