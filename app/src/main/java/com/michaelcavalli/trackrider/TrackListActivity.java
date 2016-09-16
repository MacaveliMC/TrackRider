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
import com.michaelcavalli.trackrider.util.LocationHelper;

import java.util.Date;


/**
 * This activity lists the tracks the user has entered
 */
public class TrackListActivity extends AppCompatActivity implements AddTrackDialog.DataReturnInterface,
        LoaderManager.LoaderCallbacks<Cursor>, DeleteDialog.DeleteCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = TrackListActivity.class.getSimpleName();

    private TrackListAdapter trackListAdapter;  // The adapter used to fill out the list
    private RecyclerView listOfTracks;          // The recyclerview that holds the list of tracks
    private String track_to_delete;             // The track the user wants to delete

    private GoogleApiClient mGoogleApiClient;       // The GoogleApiClient for location information
    Location mLastLocation;                         // Last known location
    private int REQUEST_PERMISSION_CODE = 8153;     // Code used if a permission request is made
    private int LOCATION_SETTINGS_RESOLUTION = 4259;// Code used if a settings change is requested

    private static final int TRACK_LIST_LOADER = 1; // The number of the loader being used

    // The projection used to get data from the DB
    private static final String[] TRACK_LIST_COLUMNS = {
            DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry._ID,
            DataContract.TrackEntry.COLUMN_TRACK_NAME,
            DataContract.TrackEntry.COLUMN_LATITUDE,
            DataContract.TrackEntry.COLUMN_LONGITUDE
    };

    // The column references used to access data on the returned cursor
    static final int COL_TRACK_ENTRY_ID = 0;
    static final int COL_TRACK_NAME = 1;
    static final int COL_TRACK_LATITUDE = 2;
    static final int COL_TRACK_LONGITUDE = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        // Start the GoogleApiClient with location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Get reference to the recyclerview
        listOfTracks = (RecyclerView) findViewById(R.id.tracklist_recycler_view);
        listOfTracks.setLayoutManager(new LinearLayoutManager(this));
        listOfTracks.setHasFixedSize(true);

        // Create the adapter used to load the recyclerview with info
        trackListAdapter = new TrackListAdapter(this, new TrackListAdapter.TrackListAdapterOnClickHandler() {
            // The onclick method used when the user wants to either delete or open a map of the track
            @Override
            public boolean onClick(TrackListAdapter.TrackListAdapterViewHolder vh, boolean longClick) {
                // If it's a long click, prepare options to delete
                if (longClick) {
                    // Get the name of the track to delete
                    track_to_delete = vh.trackName.getText().toString();
                    if (track_to_delete != null) {
                        // Show the delete dialog
                        DialogFragment newDialog = new DeleteDialog();
                        newDialog.show(getSupportFragmentManager(), getString(R.string.delete_dialog));
                        return true;
                    } else
                        return false;
                } else {
                    // If it's a short click, send an intent to maps to show the location of the track
                    if(vh.fullGpsLocation == null) {
                        Toast.makeText(TrackListActivity.this, R.string.no_gps_info, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    String geo = "geo:" + vh.fullGpsLocation;
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

        // Set the adapter for the track recyclerview
        listOfTracks.setAdapter(trackListAdapter);
        // Start the loader
        getSupportLoaderManager().initLoader(TRACK_LIST_LOADER, null, this);


    }

    /**
     * This method works to get location information once the GoogleApiClient is connected
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation = new Location("spoof location");

        // Once connected, we check if we have permission to access coarse location.
        if (LocationHelper.checkPermission(this)) {
            // If we have permission, we attempt to get the latest location from the API
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // If location is null, request updates
            if (mLastLocation == null)
                requestLocationUpdates();
        } else {
            // If we don't have permission, we attempt to get it
            LocationHelper.requestPermission(TrackListActivity.this, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            // If the permission request was a success, go ahead and request location updates
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && LocationHelper.checkPermission(this)) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                // If location is null, request updates
                if (mLastLocation == null)
                    requestLocationUpdates();
            }
        }
    }

    /**
     * This is the return method if a request was made to change location settings
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS_RESOLUTION) {
            if (resultCode == RESULT_OK) {
                requestLocationUpdates();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This method creates a location request, and checks to make sure the settings are acceptable
     * for the request.  If they are, we can request location updates.  If not, we need to request
     * it be changed.
     */
    private void requestLocationUpdates() {
        // Create the builder for the request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationHelper.getLocationRequest());

        // Check the location settings to see if they are what we need
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        // The callback for the result
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates s = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    // If the location settings are good, we check permissions again
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (LocationHelper.checkPermission(TrackListActivity.this)) {
                            // Location settings are good, and we have permission, so lets request updates
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, LocationHelper.getLocationRequest(), TrackListActivity.this);
                        } else {
                            // If we don't have permission, we need to request it.
                            LocationHelper.requestPermission(TrackListActivity.this, REQUEST_PERMISSION_CODE);
                        }
                        break;
                    // Location settings are not what we need, attempting to resolve
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Show dialog to user by calling startResolutionFoResult on the status
                        LocationHelper.resolutionForResult(TrackListActivity.this, status, LOCATION_SETTINGS_RESOLUTION);
                        break;
                    // The settings are not changeable
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    /**
     * This method is called when the location changes.  We update our location.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    /**
     * Called when the GoogleApiClient connection is suspended
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "GoogleApiClient Connection Suspended.");
    }

    /**
     * Called when the GoogleApiClient connection fails
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "GoogleApiClient Connection Failed.");
    }

    /**
     * This method is called when the FAB is clicked to add a new track.  It opens the add track
     * dialog.
     * @param view The view clicked
     */
    public void addTrack(View view) {
        DialogFragment newDialog = new AddTrackDialog();
        newDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
    }

    /**
     * When the activity starts, connect to the GoogleApiClient
     */
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * When the activity stops, disconnect from the GoogleApiClient
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * This method is called when there is a positive click on the add track dialog.  It will add
     * the entered track name and if the GPS location box is checked, it will add any GPS information
     * that is available.
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        String trackName;
        double longitude;
        double latitude;

        // The checkbox for using GPS info
        CheckBox cBox = (CheckBox) dialog.getDialog().findViewById(R.id.Location_checkBox);
        // The ContentValues for new info to be entered into DB
        ContentValues newTrackValues = new ContentValues();

        // The edittext that was used to enter a new track name
        EditText newTrackName = (EditText) dialog.getDialog().findViewById(R.id.track_name_entry);
        // Get the new track name from the EditText box
        trackName = newTrackName.getText().toString();

        // Add the track name to the ContentValues
        newTrackValues.put(DataContract.TrackEntry.COLUMN_TRACK_NAME, trackName);

        // See if the user wants to store GPS info, and check if it's available
        if (cBox.isChecked())
            // See if last location is null
            if (mLastLocation != null) {
                // If not, get location info and add to ContentValues
                longitude = mLastLocation.getLongitude();
                latitude = mLastLocation.getLatitude();
                newTrackValues.put(DataContract.TrackEntry.COLUMN_LONGITUDE, Double.toString(longitude));
                newTrackValues.put(DataContract.TrackEntry.COLUMN_LATITUDE, Double.toString(latitude));
            } else {
                // If location info is null, tell user GPS info is not available
                Toast.makeText(TrackListActivity.this, R.string.no_gps_info, Toast.LENGTH_SHORT).show();
            }

        // Put new track info into DB
        getContentResolver().insert(DataContract.TrackEntry.CONTENT_URI, newTrackValues);
    }

    /**
     * Called if the user cancels the add track dialog
     * @param dialog
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Nothing happens
    }

    /**
     * This method is called by the Delete dialog if the user long clicks on a track to delete.
     * It will delete the track along with all associated track days and sessions
     */
    @Override
    public void delete() {
        if (track_to_delete != null) {

            // Selection to delete sessions on this track
            String selection = DataContract.SessionsEntry.COLUMN_TRACK_NAME + " = ? ";
            // Selection arg that is the track name
            String[] selectionArgs = new String[]{track_to_delete};

            // Delete all sessions on this track
            getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);

            // Selection to delete track days on this track
            selection = DataContract.TrackDays.COLUMN_TRACK_NAME + " = ? ";

            // Delete all track days on this track
            getContentResolver().delete(DataContract.TrackDays.CONTENT_URI, selection, selectionArgs);

            // Selection to delete the track from track list
            selection = DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry.COLUMN_TRACK_NAME + " = ? ";

            // Delete the track from the track list
            getContentResolver().delete(DataContract.TrackEntry.CONTENT_URI, selection, selectionArgs);

            // Notify the data set has changed
            trackListAdapter.notifyDataSetChanged();
            // Set the track to delete to null
            track_to_delete = null;
        }
    }

    /**
     * Once the loader is created, it queries for track list data from the DB
     * @param id The ID of the loader
     * @param args the args passed
     * @return
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri trackListUri = DataContract.TrackEntry.buildTrackListUri();
        // Return tracks in ASC order by track name
        String sortOrder = DataContract.TrackEntry.COLUMN_TRACK_NAME + " ASC";
        return new CursorLoader(this,
                trackListUri,
                TRACK_LIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Once the loader is finished, swap the cursor
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        trackListAdapter.swapCursor(data);
    }

    /**
     * Swap the cursor our for null when the loader resets
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader loader) {
        trackListAdapter.swapCursor(null);
    }
}
