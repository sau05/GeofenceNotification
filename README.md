# GeofenceNotification
long press event is used to enable Geofencing
default radius for Geofence is 500 meters, user can enter manually once he enables Geofencing
After enabling it the broadcastereceiver will start and checks location update on every 20 minutes from LocationService.
all the location data is getting stored in local database, currently it only supports 1 Geofence.
Whenever user exits app and comeback he can see last Geofence
