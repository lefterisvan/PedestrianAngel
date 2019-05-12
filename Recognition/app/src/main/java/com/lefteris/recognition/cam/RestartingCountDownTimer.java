package com.lefteris.recognition.cam;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;


abstract class RestartingCountDownTimer {


    private final long mMillisInFuture;


    private final long mCountdownInterval;

    private long mStopTimeInFuture;


    RestartingCountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }


    public final void cancel() {
        mHandler.removeMessages(MSG);
    }


    public synchronized final RestartingCountDownTimer startOrRestart() {
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        mHandler.removeMessages(MSG);
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }


    public void onTick(long millisUntilFinished) {

    }


    public abstract void onFinish();

    private static final int MSG = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            synchronized (RestartingCountDownTimer.this) {
                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    onFinish();
                } else if (millisLeft < mCountdownInterval) {

                    sendMessageDelayed(obtainMessage(MSG), millisLeft);
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);


                    long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();


                    while (delay < 0) delay += mCountdownInterval;

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
