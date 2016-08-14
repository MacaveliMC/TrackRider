package com.michaelcavalli.trackrider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.michaelcavalli.trackrider.data.DataContract;
import com.michaelcavalli.trackrider.dialogs.AddTrackDayDialog;
import com.michaelcavalli.trackrider.dialogs.AddTrackDialog;
import com.michaelcavalli.trackrider.dialogs.DeleteDialog;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteDialog.DeleteCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private TrackDayAdapter trackDayAdapter;
    private RecyclerView listOfTrackDays;
    private int trackIdToDelete = 0;
    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private static final int TRACK_DAY_LOADER = 1;
    private int REQUEST_PERMISSION_CODE = 8153;
    private int LOCATION_SETTINGS_RESOLUTION = 4259;

    public static final String TRACK_NAME_DATA = "com.michaelcavalli.trackrider.TRACK_NAME_DATA";
    public static final String DATE_DATA = "com.michaelcavalli.trackrider.DATE_DATA";


    private static final String[] TRACK_DAY_COLUMNS = {
            DataContract.TrackDays.TABLE_NAME + "." + DataContract.TrackDays._ID,
            DataContract.TrackDays.COLUMN_TRACK_NAME,
            DataContract.TrackDays.COLUMN_TRACK_DAY_DATE
    };


    static final int COL_TRACK_ENTRY_ID = 0;
    static final int COL_TRACK_NAME = 1;
    static final int COL_TRACK_DAY_DATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        listOfTrackDays = (RecyclerView) findViewById(R.id.main_recycler_view);
        listOfTrackDays.setLayoutManager(new LinearLayoutManager(this));

        listOfTrackDays.setHasFixedSize(true);

        trackDayAdapter = new TrackDayAdapter(this, new TrackDayAdapter.TrackDayOnClickHandler() {
            @Override
            public boolean onClick(TrackDayAdapter.TrackDayViewHolder vh, boolean longClick) {

                if (longClick) {
                    trackIdToDelete = vh.id;
                    if (trackIdToDelete != 0) {
                        DialogFragment newDialog = new DeleteDialog();
                        newDialog.show(getSupportFragmentManager(), getString(R.string.delete_dialog));
                        return true;
                    } else
                        return false;
                } else {
                    openSessionList(vh.id, vh.trackName.getText().toString(), vh.date.getText().toString());
                    return true;
                }
            }
        });

        listOfTrackDays.setAdapter(trackDayAdapter);
        getSupportLoaderManager().initLoader(TRACK_DAY_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation = new Location("spoof locaton");


        Log.v(LOG_TAG, "GoogleApiClient Connected!");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        else {
            Log.v(LOG_TAG, "NO PERMISSION, STARTING LOCATION CONNECTIONS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            startLocationConnections();
        }
        if (mLastLocation != null) {
            Log.v(LOG_TAG, "Latitude is: " + mLastLocation.getLatitude());
            Log.v(LOG_TAG, "Longitude is: " + mLastLocation.getLongitude());
        } else {
            Log.v(LOG_TAG, "LOCATION IS NULL, STARTING LOCATION CONNECTIONS!!!!!!!!!!!!!!!!!");
            startLocationConnections();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, getLocationRequest(), this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS_RESOLUTION) {
            if (resultCode == RESULT_OK) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected LocationRequest getLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return mLocationRequest;
    }

    public void startLocationConnections() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates s = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.v(LOG_TAG, "SUCCESS!!!!!!!!!!!!!!!!!!!!!");
                        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CODE);
                            Log.v(LOG_TAG, "PERMISSION WASN'T GRANTED, REQUESTING IT!!!!!!!!!!!!!");
                        } else {
                            Log.v(LOG_TAG, "PERMISSION WAS GRANTED, REQUESTING LOCATION UPDATES!!!!!!!!!!!!!!");
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, getLocationRequest(), MainActivity.this);
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.v(LOG_TAG, "RESOLUTION REQUIRED!!!!!!!!!!!!!!!!!!!!!!!!!");
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    LOCATION_SETTINGS_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.v(LOG_TAG, "SETTINGS CHANGE UNAVAILABLE!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.v(LOG_TAG, "NEW LONGITUDE: " + mLastLocation.getLongitude());
        Log.v(LOG_TAG, "NEW LATITUDE: " + mLastLocation.getLatitude());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "GoogleApiClient Connection Suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "GoogleApiClient Connection Failed.");
    }

    @Override
    public void delete() {
        if (trackIdToDelete != 0) {
            int rowsDeleted;

            String selection = DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(trackIdToDelete)};

            rowsDeleted = getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);

            Log.v(LOG_TAG, "Sessions Deleted: " + rowsDeleted);

            selection = DataContract.TrackDays.TABLE_NAME + "." + DataContract.TrackDays._ID + " = ? ";

            rowsDeleted = getContentResolver().delete(DataContract.TrackDays.CONTENT_URI, selection, selectionArgs);

            Log.v(LOG_TAG, "Track Days Deleted: " + rowsDeleted);

            trackDayAdapter.notifyDataSetChanged();
            trackIdToDelete = 0;
        }
    }

    public void openSessionList(int id, String trackName, String date) {
        Uri uri = DataContract.SessionsEntry.buildSessionsWithTrackDayId(id);
        Intent intent = new Intent(this, SessionsActivity.class);
        intent.setData(uri);
        intent.putExtra(TRACK_NAME_DATA, trackName);
        intent.putExtra(DATE_DATA, date);
        startActivity(intent);
    }

    public void openTrackList(View view) {
        Intent intent = new Intent(this, TrackListActivity.class);
        startActivity(intent);
    }

    public void addTrackDay(View v) {
        Bundle args = new Bundle();
        DialogFragment addTrackDialog = new AddTrackDayDialog();

        if (mLastLocation != null) {
            ContentValues closeTrack = checkForCloseTrack();
            if (closeTrack != null) {
                Uri trackdayUri = DataContract.TrackDays.buildTrackDays();
                getContentResolver().insert(trackdayUri, closeTrack);
            } else
                addTrackDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
        } else
            addTrackDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));


    }

    public ContentValues checkForCloseTrack() {
        Cursor trackListCursor = getContentResolver().query(DataContract.TrackEntry.CONTENT_URI, null, null, null, null);

        if (trackListCursor == null)
            return null;

        trackListCursor.moveToFirst();

        for (int i = 0; i < trackListCursor.getCount(); i++) {
            String lo;
            String la;
            Location newLocation = new Location("Track Location");

            lo = trackListCursor.getString(2);
            la = trackListCursor.getString(3);


            if (lo != null && la != null) {
                Log.v(LOG_TAG, "TRACK NAME: " + trackListCursor.getString(1));
                Log.v(LOG_TAG, "TRACK LONGITUDE: " + lo);
                Log.v(LOG_TAG, "TRACK LATITUDEL: " + la);

                newLocation.setLongitude(Double.parseDouble(lo));
                newLocation.setLatitude(Double.parseDouble(la));

                float distanceTo = mLastLocation.distanceTo(newLocation);
                double distanceInMiles = distanceTo / 1609.34;

                Log.v(LOG_TAG, "DISTANCE: " + distanceInMiles);

                if (distanceInMiles < 5) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    String trackDayDate = sdf.format(new Date(System.currentTimeMillis()));

                    ContentValues cv = new ContentValues();
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_NAME, trackListCursor.getString(1));
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_KEY, trackListCursor.getInt(0));
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_DAY_DATE, trackDayDate);
                    return cv;
                }
            }
            trackListCursor.moveToNext();
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TRACK_DAY_LOADER) {
            Uri trackListUri = DataContract.TrackDays.buildTrackDays();
            String sortOrder = DataContract.TrackDays.COLUMN_TRACK_DAY_DATE + " ASC";
            return new CursorLoader(this, trackListUri, TRACK_DAY_COLUMNS, null, null, sortOrder);
        } else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        trackDayAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        trackDayAdapter.swapCursor(null);
    }
}
