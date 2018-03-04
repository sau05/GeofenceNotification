package com.saurabh.geofence_notification.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.saurabh.geofence_notification.util.AppConstants;
import com.saurabh.geofence_notification.R;
import com.saurabh.geofence_notification.service.LocationService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    GoogleMap mMap;
    private Circle mCircle;
//    private Marker mMarker;
    private BroadcastReceiver receiver;
    private double userLong,userLat,desiredLong,desiredLat;
    double radiusInMeters = 100.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.MULTIPLE_REQUEST_CODE);
            }else {
                startLocationUpdates();
            }
        }else {
            startLocationUpdates();
        }
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                userLong=intent.getDoubleExtra("Longitude",0);
                userLat=intent.getDoubleExtra("Latitude",0);
                Log.d("mainactivity",""+String.valueOf(intent.getDoubleExtra("Latitude",0)));
//                moveMap();
                checkGeofence(userLat,userLong);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(userLat,userLong)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        };
    }



    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,new IntentFilter(AppConstants.GET_LOCATION));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case AppConstants.MULTIPLE_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean coarseLocation=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean fineLocation=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (coarseLocation&&fineLocation){
                        startLocationUpdates();
                    }
                }
        }
    }

    private void startLocationUpdates(){
        Intent locationService=new Intent(getApplicationContext(),LocationService.class);
        startService(locationService);
    }

    private void stopLocationUpdates(){

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

//        mMap.clear();
//        mMap.addMarker(new MarkerOptions()
//                .position(latLng)
//        .draggable(true));
//        mMap.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(50)
//                .strokeWidth(1f)
//                .fillColor(Color.CYAN));
        desiredLat=latLng.latitude;
        desiredLong=latLng.longitude;
        if (mCircle==null){
            drawMarkerWithCircle(latLng);
        }else {
            updateMarkerWithCircle(latLng);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        userLat=marker.getPosition().latitude;
        userLong=marker.getPosition().longitude;
        moveMap();
    }

    private void moveMap(){
//        mMap.clear();
        LatLng latLng=new LatLng(userLat,userLong);
//        mMap.addMarker(new MarkerOptions()
//                .position(latLng)
//                .draggable(true)
//                .title("current"));
//        mMap.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(5)
//                .strokeWidth(0f)
//                .fillColor(0x550000FF));
        if (mCircle==null){
            drawMarkerWithCircle(latLng);
        }else {
            updateMarkerWithCircle(latLng);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
    private void updateMarkerWithCircle(LatLng position) {
        mCircle.setCenter(position);
//        mMarker.setPosition(position);
    }

    private void drawMarkerWithCircle(LatLng position){
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

//        MarkerOptions markerOptions = new MarkerOptions().position(position);
//        mMarker = mMap.addMarker(markerOptions);
    }
    private void checkGeofence(double userLat, double userLong) {
        boolean inside=checkInside(desiredLong,desiredLat,userLong,userLat);
        boolean outside=userLat>(desiredLat+radiusInMeters)||userLong>(desiredLong+radiusInMeters);
    }

    private boolean checkInside(double desiredLong, double desiredLat, double userLong, double userLat) {
        return calculateDistance(desiredLong,desiredLat,userLong,userLat)<radiusInMeters;
    }

    private double calculateDistance(double desiredLong, double desiredLat, double userLong, double userLat) {
        double c=Math.sin(Math.toRadians(desiredLat))*
                Math.sin(Math.toRadians(userLat))+
                Math.cos(Math.toRadians(desiredLat))*
                        Math.cos(Math.toRadians(userLat))*
                        Math.cos(Math.toRadians(userLong)-
                Math.toRadians(desiredLong));
        c=c>0?Math.min(1,c):Math.max(-1,c);
        return 3959*1.609*1000*Math.acos(c);
    }
}
