package com.lefteris.recognition;

import android.hardware.Camera;
import android.os.Vibrator;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.lefteris.recognition.cam.FaceDetectionCamera;
import com.lefteris.recognition.cam.FrontCameraRetriever;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.lefteris.recognition.cam.LoadFrontCameraAsyncTask;
import com.lefteris.recognition.cam.OneShotFaceDetectionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service implements SensorEventListener,FrontCameraRetriever.Listener, FaceDetectionCamera.Listener
{
    MainActivity m=new MainActivity();
    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 2;
    private static final Location mylocation=null;
    private static double lat,lng,lat2,lng2;
    public static  String address,name,address2,name2;
    public static LatLng latLng;
    public static Location mLastLocation,mLastLocation2;
    private static Context context;
    private SensorManager DSM;
    private Sensor mAccelerometer;
    public long lastUpdate = 0;
    public static float last_x, last_y, last_z;
    public static final int SHAKE_THRESHOLD = 600;
    private boolean hello=false;
    private float distance, speed;
    private MediaPlayer mPlayer;
    private Camera camera;






    @Override
    public void onCreate()
    {
        mPlayer= MediaPlayer.create(LocationService.this,R.raw.message);
        //flag=1;
        context=this;
        camera= LoadFrontCameraAsyncTask.camera;
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }


    @Override
    public void onFaceDetected() {
        hello=true;
        synthikes();


    }

    @Override
    public void onFaceTimedOut() {
        Log.e("onFaceTimedOut","Face Detect then lost message");
    }

    @Override
    public void onFaceDetectionNonRecoverableError() {
        Log.e("onFaceDetection","error_with_face_detection");
    }

    @Override
    public void onLoaded(FaceDetectionCamera camera) {
        camera.initialise(this);
    }

    @Override
    public void onFailedToLoadFaceDetectionCamera() {
        Log.wtf(TAG, "Failed to load camera, what went wrong?");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                last_x = x;
                last_y = y;
                last_z = z;
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class LocationListener implements com.google.android.gms.location.LocationListener,  android.location.LocationListener
    {


        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            mLastLocation2=new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Geocoder gc = new Geocoder(LocationService.this, Locale.getDefault());
            List<Address> proList= null;
            if(lat!=0 || lng!=0)
            {
                lat2=lat;
                lng2=lng;

            }
             lat = location.getLatitude();
             lng = location.getLongitude();
            try {
                proList = gc.getFromLocation(lat,lng,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(proList!=null && proList.size()>0)
            {
                if(address!=null || name!=null)
                {
                    address2=address;
                    name2=name;

                }
                Address addre=proList.get(0);
                address=addre.getLocality();
                name=addre.getAddressLine(0);


            }
            latLng=new LatLng(lat,lng);
            Log.e(TAG, "onLocationChanged: " + address+name);
            Log.e(TAG, "onLocationChanged: " +location);

            if(mLastLocation!=null)
            {
                mLastLocation2.set(mLastLocation);
                Log.e("Location 2",mLastLocation2+"");
            }
            mLastLocation.set(location);
            speed=mLastLocation.getSpeed();
            Log.e("onLocationChanged",mLastLocation+"");
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }


    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        DSM= (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer=DSM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        DSM.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST,new Handler());

        FrontCameraRetriever.retrieveFor(this);
        //flag=1;


        return START_STICKY;
    }



    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }



        DSM.unregisterListener(this);

        try {
            camera.stopPreview();
            camera.stopFaceDetection();
            camera.release();
        } catch (RuntimeException e) {
            Log.e("onDestroy","catch camera");

        } finally {
            if (camera != null) camera.release();
        }
        //flag=0;
        stopService(new Intent(this,FrontCameraRetriever.class));
        stopSelf();
        super.onDestroy();

    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
    public void synthikes()
    {
        if(mLastLocation2!=null)
        {
            distance=mLastLocation.distanceTo(mLastLocation2);

        }

       if (screen() == true && locker()==false)
        {

            if(isNetworkAvailable(getBaseContext())==false)
            {


                if ( (ActivitiesIntentService.isRunning == true || ActivitiesIntentService.isWalking == true || ActivitiesIntentService.onFoot==true) )
                {


                    if((speed<4 && speed>0.1)  && distance>=1.5)
                    {




                        if(hello==true)
                        {
                           mPlayer.start();
                           mPlayer.setLooping(false);
                           Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                           // Vibrate for 250 milliseconds
                            MainActivity mainActivity= new MainActivity();
                            mainActivity.getMylocation();
                           v.vibrate(250);
                            hello=false;


                        }
                    }
                }

            }


        }

    }
    public boolean screen() {
        boolean screen=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) getBaseContext().getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {

                    screen=true;

                } else screen= false;
            }
        } else {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager.isScreenOn()) {
                screen= true;
            } else screen= false;
        }  Log.e("locker",""+screen);
        return screen;

    }
    public boolean locker()
    {
        KeyguardManager kgMgr =
                (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean showing = kgMgr.inKeyguardRestrictedInputMode();


        return showing;
    }
    public static boolean isNetworkAvailable(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            boolean isWifiConn = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            if (isWifiConn==true)
            {
                status = true;
                //Toast.makeText(context,"your WiFi is  on",Toast.LENGTH_LONG).show();
            } else {
               status=false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }
}