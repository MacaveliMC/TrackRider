package com.michaelcavalli.trackrider.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by silen_000 on 9/12/2016.
 */
public class LocationHelper {

    public static boolean checkPermission(Context c) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    public static void requestPermission(Activity c, int permissionCode) {
        ActivityCompat.requestPermissions(c, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, permissionCode);
    }

    public static void resolutionForResult(Activity c, Status status, int settingsResolutionCode) {
        try {
            status.startResolutionForResult(
                    c,
                    settingsResolutionCode);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error
        }
    }

    /**
     * This returns a location request to receive location updates
     * @return
     */
    public static LocationRequest getLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return mLocationRequest;
    }
}
