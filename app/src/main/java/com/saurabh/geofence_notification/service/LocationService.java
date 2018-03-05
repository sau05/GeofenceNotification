package com.saurabh.geofence_notification.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.saurabh.geofence_notification.data.SQLiteDataProvider;
import com.saurabh.geofence_notification.data.SQLiteHelper;
import com.saurabh.geofence_notification.model.GeoItem;
import com.saurabh.geofence_notification.notification.NotificationHandler;

/**
 * Created by kiris on 3/3/2018.
 */

public class LocationService extends IntentService {

    private static final String TAG = "Locationservice";
    private static final long TWO_MINUTES = 1000*60*2;
    private LocationManager mLocationManager;
    private static final int LOCATION_INTERVAL = 0;
    private static final float LOCATION_DISTANCE = 0;
    private double radiusInMeters;
    private double desiredLong;
    private double desiredLat;
    private SQLiteDataProvider dataProvider;
    private SQLiteHelper helper;

    public LocationService() {
        super("");
    }

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation=null;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
//            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location,mLastLocation)){
                Log.e(TAG, "onLocationChanged: " + location.getLatitude());
                checkGeofence(location);
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
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            desiredLat=intent.getDoubleExtra("latitude",0);
            desiredLong=intent.getDoubleExtra("longitude",0);
            radiusInMeters=intent.getDoubleExtra("radius",0);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();

        helper=new SQLiteHelper(getApplicationContext());
        dataProvider=new SQLiteDataProvider(getApplicationContext(),helper);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        helper.close();
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

    private void checkGeofence(Location location) {
        Location desiredLocation = new Location("");
        desiredLocation.setLatitude(desiredLat);
        desiredLocation.setLongitude(desiredLong);
        boolean inside=location.distanceTo(desiredLocation)<=radiusInMeters;
        GeoItem geoItem=new GeoItem();
        geoItem.setLatitude(desiredLat);
        geoItem.setLongitude(desiredLong);
        geoItem.setRadius(radiusInMeters);
        if (dataProvider.getGeofence().isIn()){
            if (!inside){
                geoItem.setIn(false);
                geoItem.setOut(true);
                dataProvider.saveGeofence(geoItem);
                NotificationHandler.sendNotification(getApplicationContext(),"Exit","You are out of the area",1);
            }
        }else if (dataProvider.getGeofence().isOut()){
            if (inside){
                geoItem.setIn(true);
                geoItem.setOut(false);
                dataProvider.saveGeofence(geoItem);
                NotificationHandler.sendNotification(getApplicationContext(),"Enter","You are selected in area",0);
            }
        } else {
            geoItem.setIn(inside);
            geoItem.setOut(!inside);
            if(inside){
                dataProvider.saveGeofence(geoItem);
                NotificationHandler.sendNotification(getApplicationContext(),"Enter","You are selected in area",0);
            }else {
                dataProvider.saveGeofence(geoItem);
                NotificationHandler.sendNotification(getApplicationContext(),"Exit","You are out of the area",1);
            }
        }
    }
}
