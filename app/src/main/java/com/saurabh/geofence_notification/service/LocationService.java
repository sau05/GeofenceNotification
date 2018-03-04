package com.saurabh.geofence_notification.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.saurabh.geofence_notification.util.AppConstants;

/**
 * Created by kiris on 3/3/2018.
 */

public class LocationService extends Service {

    private static final String TAG = "Locationservice";
    private static final long TWO_MINUTES = 1000*60*2;
    private LocationManager mLocationManager;
    private static final int LOCATION_INTERVAL = 4000;
    private static final float LOCATION_DISTANCE = 10f;
    private Intent intent;
    private LocalBroadcastManager broadcastManager;

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation=null;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
//            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
//            mLastLocation.set(location);
            if (isBetterLocation(location,mLastLocation)){
                Log.e(TAG, "onLocationChanged: " + location.getLatitude());
                location.getLatitude();
                location.getLongitude();
                intent.putExtra("Latitude",location.getLatitude());
                intent.putExtra("Longitude",location.getLongitude());
                intent.putExtra("provider",location.getProvider());
                broadcastManager.sendBroadcast(intent);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.e(TAG, "onStatusChanged: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.e(TAG, "onProviderEnabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.e(TAG, "onProviderDisabled: " + s);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        broadcastManager= LocalBroadcastManager.getInstance(this);

        intent=new Intent(AppConstants.GET_LOCATION);
        initializeLocationManager();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
            }catch (java.lang.SecurityException e){
                e.printStackTrace();
                Log.i(TAG, "fail to request location update, ignore", e);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                Log.d(TAG, "network provider does not exist, " + e.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException e) {
                e.printStackTrace();
                Log.i(TAG, "fail to request location update, ignore", e);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.d(TAG, "gps provider does not exist " + e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager!=null){
            for (int i=0;i<mLocationListeners.length;i++){
                try{
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i(TAG, "fail to remove location listners, ignore", e);
                }
            }
        }
    }

    private void initializeLocationManager(){
        Log.e(TAG, "initializeLocationManager");
        if(mLocationManager==null){
            mLocationManager= (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        }
    }

    protected boolean isBetterLocation(Location location,Location currentBestLocation){
        if (currentBestLocation==null){
            return true;
        }
        long timeDelta=location.getTime()-currentBestLocation.getTime();
        boolean isSignificantlyNewer=timeDelta>TWO_MINUTES;
        boolean isSignificantlyOlder=timeDelta<=TWO_MINUTES;
        boolean isNewer=timeDelta>0;

        if (isSignificantlyNewer){
            return true;
        }else if(isSignificantlyOlder){
            return false;
        }

        int accuracyDelta= (int) (location.getAccuracy()-currentBestLocation.getAccuracy());
        boolean isLessAccurate=accuracyDelta>0;
        boolean isMoreAccurate=accuracyDelta<0;
        boolean isSignificantlyLessAccurate=accuracyDelta>200;

        boolean isFromSameProvider=isSameProvider(location.getProvider(),currentBestLocation.getProvider());
        if (isMoreAccurate){
            return true;
        }else if (isNewer&&!isLessAccurate){
            return true;
        }else if (isNewer&&!isSignificantlyLessAccurate&&isFromSameProvider){
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider, String provider1) {
        if (provider==null){
            return provider1==null;
        }
        return provider.equals(provider1);
    }
}
