# GeofenceNotification
# Geofencing without google play services
1.long press event is used to enable Geofencing
2.Runtime permission is handled for Android Marshmellow and above versions
2.default radius for Geofence is 250 meters
3.user can enter radius manually once he enables Geofencing
4.After enabling it the broadcastreceiver will start and checks location update on every 20 minutes from LocationService.
5.all the location data is getting stored in local database, currently it only supports 1 Geofence.
6.Whenever user exits app and comeback he can see last Geofence
