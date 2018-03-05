package com.saurabh.geofence_notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saurabh.geofence_notification.service.LocationService;

/**
 * Created by kiris on 3/4/2018.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Intent locationIntent=new Intent(context, LocationService.class);
        locationIntent.putExtra("latitude",intent.getDoubleExtra("latitude",0));
        locationIntent.putExtra("longitude",intent.getDoubleExtra("longitude",0));
        locationIntent.putExtra("radius",intent.getDoubleExtra("radius",0));
            context.startService(locationIntent);
    }
}
