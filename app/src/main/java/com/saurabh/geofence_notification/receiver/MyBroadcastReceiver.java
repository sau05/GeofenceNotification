package com.saurabh.geofence_notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.saurabh.geofence_notification.service.LocationService;

/**
 * Created by kiris on 3/4/2018.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Intent locationIntent=new Intent(context, LocationService.class);
        Log.d("broadcast",""+intent.getDoubleExtra("latitude",0));
        locationIntent.putExtra("latitude",intent.getDoubleExtra("latitude",0));
        locationIntent.putExtra("longitude",intent.getDoubleExtra("longitude",0));
            context.startService(locationIntent);
    }
}
