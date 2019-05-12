package com.lefteris.recognition.cam;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;


public class FaceDetectionCamera implements OneShotFaceDetectionListener.Listener {

    public  final Camera camera ;

    private Listener listener;

    public FaceDetectionCamera(Camera camera) {
        this.camera = camera;
    }

    public void initialise(Listener listener) {
        initialise(new DummySurfaceHolder(), listener);
    }

    private void initialise(SurfaceHolder holder, Listener listener) {
        this.listener = listener;
        try {
            camera.stopPreview();
        } catch (Exception swallow) {
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.setFaceDetectionListener(new OneShotFaceDetectionListener(this));
            camera.startFaceDetection();
        } catch (IOException e) {
            this.listener.onFaceDetectionNonRecoverableError();
        }
    }

    @Override
    public void onFaceDetected() {
        listener.onFaceDetected();
    }

    @Override
    public void onFaceTimedOut() {
        listener.onFaceTimedOut();
    }

    public void recycle() {
        if (camera != null) {
            camera.release();
        }
    }

    public interface Listener {
        void onFaceDetected();

        void onFaceTimedOut();

        void onFaceDetectionNonRecoverableError();

    }
}
