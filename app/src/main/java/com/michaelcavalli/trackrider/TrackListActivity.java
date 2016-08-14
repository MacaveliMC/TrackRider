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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
import com.michaelcavalli.trackrider.dialogs.AddTrackDialog;
import com.michaelcavalli.trackrider.dialogs.DeleteDialog;

import java.util.Date;


/**
 * Created by silen_000 on 7/28/2016.
 */
public class TrackListActivity extends AppCompatActivity implements AddTrackDialog.DataReturnInterface,
        LoaderManager.LoaderCallbacks<Cursor>, DeleteDialog.DeleteCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = TrackListActivity.class.getSimpleName();
    private TrackListAdapter trackListAdapter;
    private RecyclerView listOfTracks;
    private String track_to_delete;

    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private int REQUEST_PERMISSION_CODE = 8153;
    private int LOCATION_SETTINGS_RESOLUTION = 4259;

    private static final int TRACK_LIST_LOADER = 1;

    private static final String[] TRACK_LIST_COLUMNS = {
            DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry._ID,
            DataContract.TrackEntry.COLUMN_TRACK_NAME,
            DataContract.TrackEntry.COLUMN_LATITUDE,
            DataContract.TrackEntry.COLUMN_LONGITUDE
    };


    static final int COL_TRACK_ENTRY_ID = 0;
    static final int COL_TRACK_NAME = 1;
    static final int COL_TRACK_LATITUDE = 2;
    static final int COL_TRACK_LONGITUDE = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        listOfTracks = (RecyclerView) findViewById(R.id.tracklist_recycler_view);
        listOfTracks.setLayoutManager(new LinearLayoutManager(this));

        listOfTracks.setHasFixedSize(true);

        trackListAdapter = new TrackListAdapter(this, new TrackListAdapter.TrackListAdapterOnClickHandler() {
            @Override
            public boolean onClick(TrackListAdapter.TrackListAdapterViewHolder vh, boolean longClick) {
                if (longClick) {
                    track_to_delete = vh.trackName.getText().toString();
                    if (track_to_delete != null) {
                        DialogFragment newDialog = new DeleteDialog();
                        newDialog.show(getSupportFragmentManager(), getString(R.string.delete_dialog));
                        return true;
                    } else
                        return false;
                } else {
                    String geo = "geo:" + vh.fullGpsLocation;
                    Toast.makeText(TrackListActivity.this, geo, Toast.LENGTH_SHORT).show();
                    Uri gmmIntentUri = Uri.parse(geo);
                    Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    intent.setPackage("com.google.android.apps.maps");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    return false;
                }
            }
        });

        listOfTracks.setAdapter(trackListAdapter);
        getSupportLoaderManager().initLoader(TRACK_LIST_LOADER, null, this);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
                        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(TrackListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ActivityCompat.requestPermissions(TrackListActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CODE);
                            Log.v(LOG_TAG, "PERMISSION WASN'T GRANTED, REQUESTING IT!!!!!!!!!!!!!");
                        } else {
                            Log.v(LOG_TAG, "PERMISSION WAS GRANTED, REQUESTING LOCATION UPDATES!!!!!!!!!!!!!!");
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, getLocationRequest(), TrackListActivity.this);
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
                                    TrackListActivity.this,
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


    public void addTrack(View view) {
        DialogFragment newDialog = new AddTrackDialog();
        newDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
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
    public void onDialogPositiveClick(DialogFragment dialog) {
        String trackName;
        Uri insertUri;
        double longitude;
        double latitude;
        CheckBox cBox = (CheckBox) dialog.getDialog().findViewById(R.id.Location_checkBox);
        ContentValues newTrackValues = new ContentValues();

        EditText newTrackName = (EditText) dialog.getDialog().findViewById(R.id.track_name_entry);
        trackName = newTrackName.getText().toString();

        newTrackValues.put(DataContract.TrackEntry.COLUMN_TRACK_NAME, trackName);
        if (cBox.isChecked())
            if (mLastLocation != null) {
                longitude = mLastLocation.getLongitude();
                latitude = mLastLocation.getLatitude();
                newTrackValues.put(DataContract.TrackEntry.COLUMN_LONGITUDE, Double.toString(longitude));
                newTrackValues.put(DataContract.TrackEntry.COLUMN_LATITUDE, Double.toString(latitude));
            } else {
                Toast.makeText(TrackListActivity.this, R.string.no_gps_info, Toast.LENGTH_SHORT).show();
            }
        insertUri = getContentResolver().insert(DataContract.TrackEntry.CONTENT_URI, newTrackValues);


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.v(LOG_TAG, "NEGATIVE CLICK!!!");
    }

    @Override
    public void delete() {
        if (track_to_delete != null) {
            int rowsDeleted;

            String selection = DataContract.SessionsEntry.COLUMN_TRACK_NAME + " = ? ";
            String[] selectionArgs = new String[]{track_to_delete};

            rowsDeleted = getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);

            Log.v(LOG_TAG, "Sessions Deleted: " + rowsDeleted);

            selection = DataContract.TrackDays.COLUMN_TRACK_NAME + " = ? ";
            selectionArgs = new String[]{track_to_delete};

            rowsDeleted = getContentResolver().delete(DataContract.TrackDays.CONTENT_URI, selection, selectionArgs);

            Log.v(LOG_TAG, "Track Days Deleted: " + rowsDeleted);

            selection = DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry.COLUMN_TRACK_NAME + " = ? ";

            rowsDeleted = getContentResolver().delete(DataContract.TrackEntry.CONTENT_URI, selection, selectionArgs);
            Log.v(LOG_TAG, "Successfully deleted " + track_to_delete + " with number of rows deleted equal to " + rowsDeleted);
            trackListAdapter.notifyDataSetChanged();
            track_to_delete = null;
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri trackListUri = DataContract.TrackEntry.buildTrackListUri();
        String sortOrder = DataContract.TrackEntry.COLUMN_TRACK_NAME + " ASC";
        return new CursorLoader(this,
                trackListUri,
                TRACK_LIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        trackListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        trackListAdapter.swapCursor(null);
    }
}
