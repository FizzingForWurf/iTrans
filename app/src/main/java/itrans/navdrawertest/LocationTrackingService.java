package itrans.navdrawertest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LocationTrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private boolean hasArrived = false;

    //selected data
    private String destinationLatLng;
    private String alertRadius;
    private String alertRingTone;
    private LatLng selectedLocation;
    private float distance;

    //Google API client stuff
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng LastLatLng;
    private Location mLastLocation;

    public LocationTrackingService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show();
        alertRadius = intent.getStringExtra("AlertRadius");
        destinationLatLng = intent.getStringExtra("AlertDestination");
        alertRingTone = intent.getStringExtra("AlertRingTone");

        String[] latANDlong =  destinationLatLng.split(",");
        double latitude = Double.parseDouble(latANDlong[0]);
        double longitude = Double.parseDouble(latANDlong[1]);
        selectedLocation = new LatLng(latitude, longitude);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdate();
        Toast.makeText(this, "Service stopped.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        if (destinationLatLng != null && alertRadius != null) {

            checkDistanceFromDestination();

            float radius = Float.parseFloat(alertRadius);
            hasArrived = (distance >= radius);
        }

        //check if arriving
        if (hasArrived){
            hasArrived = false;
            startAlarm();
        }
    }

    private void startAlarm() {
        AlarmReceiver alarm = new AlarmReceiver();
        alarm.StartAlarm(this, alertRingTone);
    }

    private void checkDistanceFromDestination() {
        if (LastLatLng != null) {

            Location locationDestination = new Location("Destination");
            locationDestination.setLatitude(selectedLocation.latitude);
            locationDestination.setLongitude(selectedLocation.longitude);

            distance = mLastLocation.distanceTo(locationDestination);
        }else{
            Toast.makeText(getApplicationContext(), "Please turn on location services", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void startLocationUpdate() {
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(7000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }
}
