package red.spider.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import red.spider.util.Watch;

public class ClassicWatchFaceService extends CanvasWatchFaceService {

    // Logging Tag
    private static final String TAG = "BackupWatchFaceService";

    // Update Rates
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1) / 8;
    private static final long AMBIENT_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    @Override
    public Engine onCreateEngine() { return new Engine(); }

    // Custom Watch Engine
    private class Engine extends CanvasWatchFaceService.Engine {

        // Message Tags
        private static final int MSG_UPDATE_TIME = 0;

        Resources mResources = getResources();

        // Calendar
        private Calendar mCalendar;

        // Flags
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mRegisteredTimeZoneReceiver;

        // Main Watch
        private Watch mWatch;

        // Widgets


        // Time Zone Receiver
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        // Time Update Handler
        private final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {

                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "updating time");
                }

                invalidate();

                if (shouldTimerBeRunning()) {
                    long timeMs = System.currentTimeMillis();
                    long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                    mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                }

            }
        };

        // Center of Screen
        private float mCenterX;
        private float mCenterY;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Initialize Calendar
            mCalendar = Calendar.getInstance();

            // Initialize Main Watch
            mWatch = new Watch(mResources, mCalendar);
            mWatch.initialize(
                    R.drawable.background,
                    R.drawable.hour,
                    R.drawable.minute,
                    R.drawable.second,
                    R.drawable.date_text_bg
            );

            // Initialize Widgets


            // Configure System UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(ClassicWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (inAmbientMode)
                mAmbient = true;
            else
                mAmbient = false;

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
            }

            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            // Get Time
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            // Draw Static Background
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                mWatch.drawAmbient(canvas);
            } else {
                mWatch.draw(canvas);
            }

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update Time Zone
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

            // Update Timer
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            // Find Center
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            // Center Watch
            mWatch.setCenter(mCenterX, mCenterY);

            // Scale Watch
            mWatch.scale(width, height);

            super.onSurfaceChanged(holder, format, width, height);
        }

        // Register for Time Zone Change
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }

            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            ClassicWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        // Unregister Time Zone Receiver
        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }

            mRegisteredTimeZoneReceiver = false;
            ClassicWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        // Update Timer Status
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        // Only Run While Active
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

    }
}
