package red.spider.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;

public class Watch {

    // Paints
    private Paint mBackgroundPaint;

    // Main Bitmaps
    private Bitmap mBackgroundBitmap;
    private Bitmap mHourHandBitmap;
    private Bitmap mMinuteHandBitmap;
    private Bitmap mSecondHandBitmap;

    // Context & Resources
    private Context mContext;
    private Resources mResources;

    // Width & Height
    private int mWidth;
    private int mHeight;

    // Calendar
    private Calendar mCalendar;

    // Watch Center Coordinate
    private float mCenterX;
    private float mCenterY;

    public Watch(Resources resources, Calendar calendar) {
        mResources = resources;
        mCalendar = calendar;
    }

    // specified by user
    public void initialize(int backgroundResID, int hourResID, int minuteResID, int secondResID, int dateDayBackgroundResID) {
        mBackgroundBitmap = BitmapFactory.decodeResource(mResources, backgroundResID);
        mHourHandBitmap = BitmapFactory.decodeResource(mResources, hourResID);
        mMinuteHandBitmap = BitmapFactory.decodeResource(mResources, minuteResID);
        mSecondHandBitmap = BitmapFactory.decodeResource(mResources, secondResID);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setFilterBitmap(true);
    }

    // user specified scale
    public void scale(int width, int height) {

        // Scale Loaded Background, Hour/Minute/Second Hand Images
        float scaleBackground = ((float) height) / (float) mBackgroundBitmap.getHeight();
        float scaleHourHand = ((float) height) / (float) mHourHandBitmap.getHeight();
        float scaleMinuteHand = ((float) height) / (float) mMinuteHandBitmap.getHeight();
        float scaleSecondHand = ((float) height) / (float) mSecondHandBitmap.getHeight();

        mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                (int) (mBackgroundBitmap.getWidth() * scaleBackground),
                (int) (mBackgroundBitmap.getHeight() * scaleBackground),
                true);

        mHourHandBitmap = Bitmap.createScaledBitmap(
                mHourHandBitmap,
                (int) (mHourHandBitmap.getWidth() * scaleHourHand),
                (int) (mHourHandBitmap.getHeight() * scaleHourHand),
                true);

        mMinuteHandBitmap = Bitmap.createScaledBitmap(
                mMinuteHandBitmap,
                (int) (mMinuteHandBitmap.getWidth() * scaleMinuteHand),
                (int) (mMinuteHandBitmap.getHeight() * scaleMinuteHand),
                true);

        mSecondHandBitmap = Bitmap.createScaledBitmap(
                mSecondHandBitmap,
                (int) (mSecondHandBitmap.getWidth() * scaleSecondHand),
                (int) (mSecondHandBitmap.getHeight() * scaleSecondHand),
                true);
    }

    public void draw(Canvas canvas) {

        // Rotation Calculations
        final float seconds = mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f;
        final float secondsRotation = seconds * 6f;
        final float minutes = mCalendar.get(Calendar.MINUTE) + (seconds / 60);
        final float minutesRotation = minutes * 6f;
        final float hours = mCalendar.get(Calendar.HOUR) + (minutes / 60);
        final float hoursRotation = hours * 30f;

        // Canvas Rotation & Hand Draw
        canvas.save();

        // Draw Background
        canvas.drawBitmap(mBackgroundBitmap,
                mCenterX - (mBackgroundBitmap.getWidth() / 2),
                mCenterY - (mBackgroundBitmap.getWidth() / 2),
                mBackgroundPaint
        );

        // Hour Hand
        canvas.rotate(hoursRotation, mCenterX, mCenterY);
        canvas.drawBitmap(
                mHourHandBitmap,
                mCenterX - mHourHandBitmap.getWidth() / 2,
                mCenterY - mHourHandBitmap.getHeight() / 2,
                mBackgroundPaint
        );

        // Minute Hand
        canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
        canvas.drawBitmap(mMinuteHandBitmap,
                mCenterX - mMinuteHandBitmap.getWidth() / 2,
                mCenterY - mMinuteHandBitmap.getHeight() / 2,
                mBackgroundPaint
        );

        // Second Hand
        canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
        canvas.drawBitmap(mSecondHandBitmap,
                mCenterX - mSecondHandBitmap.getWidth() / 2,
                mCenterY - mSecondHandBitmap.getHeight() / 2,
                mBackgroundPaint
        );

        canvas.restore();
    }

    // Set Center
    public void setCenter(float x, float y) {
        mCenterX = x;
        mCenterY = y;
    }

    // Ambient Draw Mode
    public void drawAmbient(Canvas canvas) {

        // Implement alternative graphics
        canvas.drawColor(Color.BLACK);
    }

}
