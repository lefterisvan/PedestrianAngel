package com.lefteris.recognition.cam;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class FrontCameraRetriever extends Service implements Application.ActivityLifecycleCallbacks, LoadFrontCameraAsyncTask.Listener {

    private final Listener listener;


    private FaceDetectionCamera camera;

    public static void retrieveFor(Service activity)
    {

        Log.e("retreiveFor","im in");
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Your activity needs to implement FrontCameraRetriever.Listener");
        }
        Listener listener = (Listener) activity;
        retrieve(activity, listener);
    }

    private static void retrieve(Context context, Listener listener) {

        Application application = (Application) context.getApplicationContext();
        FrontCameraRetriever frontCameraRetriever = new FrontCameraRetriever(listener);
        application.registerActivityLifecycleCallbacks(frontCameraRetriever);
    }

    FrontCameraRetriever(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {

            camera.recycle();
        }
        this.getApplication().unregisterActivityLifecycleCallbacks(this);
        stopSelf();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        new LoadFrontCameraAsyncTask(this).load();
    }

    @Override
    public void onLoaded(FaceDetectionCamera camera) {
        this.camera = camera;
        listener.onLoaded(camera);
    }

    @Override
    public void onFailedToLoadFaceDetectionCamera() {
        listener.onFailedToLoadFaceDetectionCamera();
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {


    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        super.onDestroy();
        if (camera != null) {
            camera.recycle();
        }
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface Listener extends LoadFrontCameraAsyncTask.Listener {

    }
}
