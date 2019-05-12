package com.lefteris.recognition;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,ResultCallback<Status>,com.google.android.gms.location.LocationListener {


    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiC;
    private LatLng latLng,latLng2;
    private String address,name,address2,name2;
    private LocationManager lm, lm2;
    private Location mylocation;
    private  float speed=0;
    private static final String TAG = "MainActivity";
    public static GoogleApiClient mGoogleApiClient;
    private Intent safetyService,intent;
    private Marker marker;
    private LocationRequest mLocReq;
    PendingIntent pendingIntent;

    public Location getMylocation() {
        return mylocation;
    }

    public String getAddress() {
        return address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lm2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        safetyService= new Intent(MainActivity.this,LocationService.class);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();



        initMap();

        Button speedbtn =(Button) findViewById(R.id.speed);
        speedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Your current speed is "+speed,Toast.LENGTH_LONG).show();

            }
        });


        Button btnTraffic= (Button) findViewById(R.id.traffic);
        btnTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTrafficEnabled()) {
                    mGoogleMap.setTrafficEnabled(true);
                    Toast.makeText(getBaseContext(), "If you cant see traffic layers propably,\n" +
                            "there are no data traffic enabled for your location", Toast.LENGTH_LONG).show();
                } else {
                    mGoogleMap.setTrafficEnabled(false);
                }
            }
        });



        Switch SafetySwitch = (Switch) findViewById(R.id.btnSafety);

        SafetySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                if(on)
                {
                    //otan to switch gia to safety mode einai epilegmeno
                    mGoogleApiClient.connect();
                    intent = new Intent( MainActivity.this, ActivitiesIntentService.class );
                    pendingIntent = PendingIntent.getService( MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                    ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 1000, pendingIntent );
                    startService(safetyService);
                    Toast.makeText(getBaseContext(),"SAFETY MODE HAS BEEN STARTED",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //otan to switch gia to safety mode den einai epilegmeno
                    if(isMyServiceRunning(LocationService.class)==true)
                    {
                        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,pendingIntent ).setResultCallback(MainActivity.this);

                        stopService(safetyService);
                        Toast.makeText(getBaseContext(),"SAFETY MODE STOPED",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(),"There is no service running",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });





    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);

    }


    public final boolean isTrafficEnabled() {
        return mGoogleMap.isTrafficEnabled();

    }



    @Override
    public void onConnected(Bundle bundle) {

            mLocReq = LocationRequest.create();//dimioyrgei thn stigmiaia topothsia
            mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//theloume thn kalyteri dynath topothesia me thn megaliteri akibia toy xrhsth
            mLocReq.setInterval(20000);//ananewnw thn topothseisa t xrhsth kathe 20 sec
            mLocReq.setFastestInterval(1000);
            mLocReq.setSmallestDisplacement(10);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }


            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiC, mLocReq,  this);




    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
        Log.i(TAG, "Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }






    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.e(TAG, "Successfully added activity detection.");

        } else {
            Log.e(TAG, "Error: " + status.getStatusMessage());
        }
    }






    private boolean isMyServiceRunning(Class<LocationService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }




        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGoogleApiC = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiC.connect();

        Criteria criteria = new Criteria();
        String provider = lm.getBestProvider(criteria, true);
        mGoogleMap.setMyLocationEnabled(true);
        mylocation = lm.getLastKnownLocation(provider);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (marker != null) {
            marker.remove();
        }
        if(location==null)
        {
            Toast.makeText(this,"Cant get current location",Toast.LENGTH_LONG).show();

        }
        else
        {
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            CameraUpdate update= CameraUpdateFactory.newLatLngZoom(ll,17);
            mGoogleMap.animateCamera(update);
            Geocoder gc = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> proList= null;
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            try {
                if(address!=null && name!=null)
                {
                    address2=address;
                    name2=name;

                }

                proList = gc.getFromLocation(lat,lng,1);
                if(proList!=null && proList.size()>0)
                {
                    Address addre=proList.get(0);
                    address=addre.getLocality();
                    name=addre.getAddressLine(0);


                }
                latLng=new LatLng(lat,lng);
            } catch (IOException e) {
                e.printStackTrace();
            }
            speed=location.getSpeed();
         try{
             marker=mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("You are at :"+address).snippet("odos "+name+"/n your speed "+speed));
         }
         catch (IllegalArgumentException e)
         {
             Toast.makeText(getBaseContext(),"Marker is not available",Toast.LENGTH_LONG).show();

         }



        }



    }


}




