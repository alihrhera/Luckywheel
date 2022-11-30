package com.wndo.luckywheel;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.List;

/**
 * Created by mohamed on 22/04/17.
 */

final class WheelView extends View {
    private RectF range = new RectF();
    private Paint archPaint, textPaint;
    private int padding, radius, center, mWheelBackground, mImagePadding;
    private List<WheelItem> mWheelItems;
    private OnLuckyWheelReachTheTarget mOnLuckyWheelReachTheTarget;
    private OnRotationListener onRotationListener;

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initComponents() {
        //arc paint object
        archPaint = new Paint();
        archPaint.setAntiAlias(true);
        archPaint.setDither(true);
        //text paint object
        textPaint = new Paint();
//        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        if (screenSize >= 320 && screenSize < 420) {
            textPaint.setTextSize(18);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textPaint.setLinearText(true);
                textPaint.setLetterSpacing(.1f);
            }
        } else if (screenSize >= 420 && screenSize < 520) {
            textPaint.setTextSize(24);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textPaint.setLinearText(true);
                textPaint.setLetterSpacing(.25f);
            }
        } else if (screenSize >= 520) {
            textPaint.setTextSize(26);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textPaint.setLinearText(true);
                textPaint.setLetterSpacing(.35f);
            }
        }
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        textPaint.setTypeface(typeface);
        textPaint.setFakeBoldText(true);

        //rect rang of the arc
        range = new RectF(padding, padding, padding + radius, padding + radius);

    }

    /**
     * Get the angele of the target
     *
     * @return Number of angle
     */
    private float getAngleOfIndexTarget(int target) {

        return ((float) 360 / mWheelItems.size()) * target;
    }

    /**
     * Function to set wheel background
     *
     * @param wheelBackground Wheel background color
     */
    public void setWheelBackgoundWheel(int wheelBackground) {
        mWheelBackground = wheelBackground;
        invalidate();
    }

    public void setItemsImagePadding(int imagePadding) {
        mImagePadding = imagePadding;
        invalidate();
    }

    /**
     * Function to set wheel listener
     *
     * @param onLuckyWheelReachTheTarget target reach listener
     */
    public void setWheelListener(OnLuckyWheelReachTheTarget onLuckyWheelReachTheTarget) {
        mOnLuckyWheelReachTheTarget = onLuckyWheelReachTheTarget;
    }

    /**
     * Function to add wheels items
     *
     * @param wheelItems Wheels model item
     */
    public void addWheelItems(List<WheelItem> wheelItems) {
        mWheelItems = wheelItems;
        invalidate();
    }

    /**
     * Function to draw wheel background
     *
     * @param canvas Canvas of draw
     */
    private void drawWheelBackground(Canvas canvas) {
        Paint backgroundPainter = new Paint();
        backgroundPainter.setAntiAlias(true);
        backgroundPainter.setDither(true);
        backgroundPainter.setColor(mWheelBackground);
        canvas.drawCircle(center, center, center, backgroundPainter);
    }

    /**
     * Function to draw image in the center of arc
     *
     * @param canvas    Canvas to draw
     * @param tempAngle Temporary angle
     * @param bitmap    Bitmap to draw
     */
    private void drawImage(Canvas canvas, float tempAngle, Bitmap bitmap) {
        //get every arc img width and angle
        int imgWidth = (radius / mWheelItems.size()) - mImagePadding;
        float angle = (float) ((tempAngle + 360 / mWheelItems.size() / 2) * Math.PI / 180);
        //calculate x and y
        int x = (int) (center + radius / 2 / 2 * Math.cos(angle));
        int y = (int) (center + radius / 2 / 2 * Math.sin(angle));
        //create arc to draw
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        //rotate main bitmap
        float px = rect.exactCenterX();
        float py = rect.exactCenterY();
        Matrix matrix = new Matrix();
        matrix.postTranslate((float) -bitmap.getWidth() / 2, (float) -bitmap.getHeight() / 2);
        matrix.postRotate(tempAngle + 120);
        matrix.postTranslate(px, py);
        canvas.drawBitmap(bitmap, matrix, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG));
        matrix.reset();
    }

    /**
     * Function to draw text below image
     *
     * @param canvas     Canvas to draw
     * @param tempAngle  Temporary angle
     * @param sweepAngle current index angle
     * @param text       string to show
     */
    private void drawText(Canvas canvas, float tempAngle, float sweepAngle, String text, Boolean isSelect) {

        Path path = new Path();
        path.addArc(range, tempAngle, sweepAngle);
        if (isSelect) textPaint.setColor(Color.BLACK);
        else textPaint.setColor(Color.WHITE);
        float textWidth = textPaint.measureText(text);
        int hOffset = (int) (radius * Math.PI / mWheelItems.size() / 2 - textWidth / 2);
        int vOffset = ((radius / 2) / 3) - 35;
//        Log.e("TEST V vOffset", "test " + vOffset);
        canvas.drawTextOnPath(text, path, hOffset, vOffset, textPaint);
    }


    int screenSize = 0;

    public void setScreenWidth(int src) {
        screenSize = src;
    }


    int DEFAULT_ROTATION_TIME = 9000;

    /**
     * Function to rotate wheel to target
     *
     * @param target target number
     */
    public void rotateWheelToTarget(int target) {
        float wheelItemCenter = 270 - getAngleOfIndexTarget(target) + ((float) (360 / mWheelItems.size()) / 2);
        float rotation = (360 * ((int) (DEFAULT_ROTATION_TIME / 100))) + wheelItemCenter;
        float rotation2 = (360 * 28) + wheelItemCenter;
        long t = Calendar.getInstance().getTimeInMillis();
        Log.e("rotation", "DEFAULT_ROTATION_TIME " + DEFAULT_ROTATION_TIME + " rotation " + rotation + " rotation2 " + rotation2);
        animate().setInterpolator(new DecelerateInterpolator())
                .setDuration(DEFAULT_ROTATION_TIME)
                .rotation(rotation2)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        long t2 = Calendar.getInstance().getTimeInMillis();
                        Log.e("t2", "DEFAULT_ROTATION_TIME " + (t2 - t));

                        if (mOnLuckyWheelReachTheTarget != null) {
                            mOnLuckyWheelReachTheTarget.onReachTarget();
                            if (target - 1 > 0) {
                                mWheelItems.get(target - 1).setSelect(true);
                            }
                            invalidate();
                        }
                        if (onRotationListener != null) {
                            onRotationListener.onFinishRotation();
                        }
                        clearAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    /**
     * Function to rotate to zero angle
     *
     * @param target target to reach
     */
    public void resetRotationLocationToZeroAngle(final int target) {
        animate().setDuration(0)
                .rotation(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        rotateWheelToTarget(target);

                        clearAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawWheelBackground(canvas);
        initComponents();

        float tempAngle = 0;
        float sweepAngle = (float) 360 / mWheelItems.size();

        for (int i = 0; i < mWheelItems.size(); i++) {

            archPaint.setColor(mWheelItems.get(i).color);
            canvas.drawArc(range, tempAngle, sweepAngle, true, archPaint);
//            drawImage(canvas, tempAngle, mWheelItems.get(i).isSelect ? tintImage(mWheelItems.get(i).bitmap) : mWheelItems.get(i).bitmap);
            drawImage(canvas, tempAngle, mWheelItems.get(i).bitmap);
            drawText(canvas, tempAngle, sweepAngle, mWheelItems.get(i).text == null ? "" : mWheelItems.get(i).text, mWheelItems.get(i).isSelect);
            tempAngle += sweepAngle;
        }

    }

    public static Bitmap tintImage(Bitmap bitmap) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        int DEFAULT_PADDING = 5;
        padding = getPaddingLeft() == 0 ? DEFAULT_PADDING : getPaddingLeft();
        radius = width - padding * 2;
        center = width / 2;
        setMeasuredDimension(width, width);
    }

    public void setOnRotationListener(OnRotationListener onRotationListener) {
        this.onRotationListener = onRotationListener;
    }
}
