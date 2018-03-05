package com.saurabh.geofence_notification.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.saurabh.geofence_notification.R;
import com.saurabh.geofence_notification.data.SQLiteDataProvider;
import com.saurabh.geofence_notification.data.SQLiteHelper;
import com.saurabh.geofence_notification.model.GeoItem;
import com.saurabh.geofence_notification.receiver.MyBroadcastReceiver;
import com.saurabh.geofence_notification.util.AppConstants;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {

    GoogleMap mMap;
    private Circle mCircle;
//    private BroadcastReceiver receiver;
    private double desiredLong,desiredLat;
    double radiusInMeters = 500.0;
    private EditText editRadius;
    private Button btnSubmit;
    private Snackbar mSnackbar;
    private View view;
//    private boolean fineLocation;
//    private boolean coarseLocation;
    private SQLiteHelper sqLiteHelper;
    private SQLiteDataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view=findViewById(R.id.constraintLayout);
        editRadius=findViewById(R.id.editText);
        btnSubmit=findViewById(R.id.button);
        btnSubmit.setOnClickListener(this);

        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sqLiteHelper=new SQLiteHelper(getApplicationContext());
        dataProvider=new SQLiteDataProvider(getApplicationContext(),sqLiteHelper);

//        receiver=new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                userLong=intent.getDoubleExtra("Longitude",0);
//                userLat=intent.getDoubleExtra("Latitude",0);
//                Log.d("mainactivity",""+String.valueOf(intent.getDoubleExtra("Latitude",0)));
////                moveMap();
////                checkGeofence(userLat,userLong);
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(userLat,userLong)));
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//            }
//        };
    }

    @Override
    protected void onStart() {
        super.onStart();
//        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,new IntentFilter(AppConstants.GET_LOCATION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSnackbar=Snackbar.make(view,"Long press to active geofence",Snackbar.LENGTH_LONG);
        mSnackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteHelper.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case AppConstants.PERMISSION_REQUEST_CODE:
                if (grantResults.length>0){
//                    boolean coarseLocation=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean fineLocation=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (fineLocation){
                        startBroadcast();
                    }
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
//        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        GeoItem geoItem=dataProvider.getGeofence();
        if (geoItem!=null){
            Log.d("mainactivity", ""+(geoItem.getLatitude()));
            radiusInMeters=geoItem.getRadius();
            editRadius.setFocusableInTouchMode(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(geoItem.getLatitude(),geoItem.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            if (mCircle==null){
                drawMarkerWithCircle(new LatLng(geoItem.getLatitude(),geoItem.getLongitude()));
            }else {
                updateMarkerWithCircle(new LatLng(geoItem.getLatitude(),geoItem.getLongitude()));
            }
        }else {
            editRadius.setFocusable(false);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        desiredLat=latLng.latitude;
        desiredLong=latLng.longitude;
        editRadius.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.PERMISSION_REQUEST_CODE);
            }else {
                startBroadcast();
            }
        }else {
            startBroadcast();
        }
        if (mCircle==null){
            drawMarkerWithCircle(latLng);
        }else {
            updateMarkerWithCircle(latLng);
        }
    }

//    private void moveMap(){
//        LatLng latLng=new LatLng(userLat,userLong);
//        if (mCircle==null){
//            drawMarkerWithCircle(latLng);
//        }else {
//            updateMarkerWithCircle(latLng);
//        }
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//    }
    private void updateMarkerWithCircle(LatLng position) {
        mCircle.setCenter(position);
    }

    private void drawMarkerWithCircle(LatLng position){
        int strokeColor = 0xffff0000;
        int shadeColor = 0x44ff0000;

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

    }

    private void startBroadcast(){
        AlarmManager alarmManager;
        PendingIntent pendingIntent;
        alarmManager= (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        Intent intent=new Intent(this, MyBroadcastReceiver.class);
        intent.putExtra("latitude",desiredLat);
        intent.putExtra("longitude",desiredLong);
        pendingIntent=PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),100*6*2,pendingIntent);
        }
        GeoItem geoItem=new GeoItem();
        geoItem.setLatitude(desiredLat);
        geoItem.setLongitude(desiredLong);
        geoItem.setRadius(radiusInMeters);
        geoItem.setIn(false);
        geoItem.setOut(false);
        dataProvider.saveGeofence(geoItem);
    }

    @Override
    public void onClick(View view) {
        radiusInMeters= Double.parseDouble(editRadius.getText().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.PERMISSION_REQUEST_CODE);
            }else {
                startBroadcast();
            }
        }else {
            startBroadcast();
        }
    }
}
