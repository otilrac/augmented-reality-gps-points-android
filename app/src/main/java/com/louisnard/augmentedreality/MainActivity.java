package com.louisnard.augmentedreality;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Main activity showing {@link Compass} data in a {@link CompassView}.
 *
 * @author Alexandre Louisnard
 */

public class MainActivity extends AppCompatActivity implements Compass.CompassListener, LocationListener {

    // Tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Views
    private CompassView mCompassView;

    // Compass
    private Compass mCompass;

    // Location
    private LocationManager mLocationManager;

    // Request codes
    private final int REQUEST_PERMISSIONS = 1;

    // TODO: enum ?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
            }
            Log.d(TAG, "Missing permissions.");
            return;
        }

        // Views
        mCompassView = (CompassView) findViewById(R.id.compass_view);

        // Compass
        mCompass = Compass.getInstance(this, this);
        if (mCompass == null) {
            Log.d(TAG, "The device does not have the required sensors to use a augmentedreality.");
        }

        // Location
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCompass != null) mCompass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCompass != null) mCompass.stop();
    }

    // CompassListener interface
    @Override
    public void onAzimuthChanged(float azimuth) {
        mCompassView.updateAzimuth(azimuth);
    }

    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());
    }

    // LocationListener interface
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // LocationListener interface
    @Override
    public void onProviderEnabled(String provider) {

    }

    // LocationListener interface
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                recreate();
            }
        }
    }
}
