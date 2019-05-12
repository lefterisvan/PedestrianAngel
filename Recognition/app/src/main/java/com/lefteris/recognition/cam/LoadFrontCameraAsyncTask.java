package com.lefteris.recognition.cam;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;


public class LoadFrontCameraAsyncTask extends AsyncTask<Void, Void, FaceDetectionCamera> {

    private static final String TAG = "FDT" + LoadFrontCameraAsyncTask.class.getSimpleName();

    private final Listener listener;
    public  static Camera camera;

    public LoadFrontCameraAsyncTask(Listener listener) {
        this.listener = listener;
    }

    public void load() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected FaceDetectionCamera doInBackground(Void... params) {
        try {
            int id = getFrontFacingCameraId();
             camera = Camera.open(id);

            if (camera.getParameters().getMaxNumDetectedFaces() == 0) {
                Log.e(TAG, "Face detection not supported");
                return null;
            }

            return new FaceDetectionCamera(camera);
        } catch (RuntimeException e) {
            Log.e(TAG, "Likely hardware / non released camera / other app fail", e);
            return null;
        }
    }

    private int getFrontFacingCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int i = 0;
        for (; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                break;
            }
        }
        return i;
    }

    @Override
    protected void onPostExecute(FaceDetectionCamera camera) {
        super.onPostExecute(camera);
        if (camera == null) {
            listener.onFailedToLoadFaceDetectionCamera();
        } else {
            listener.onLoaded(camera);
        }
    }

    public interface Listener {
        void onLoaded(FaceDetectionCamera camera);

        void onFailedToLoadFaceDetectionCamera();
    }

}
