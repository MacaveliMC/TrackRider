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

/**
 * This is the app main activity.  It displays a list of Track Days, and includes a button to the
 * track list screen.
 */

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteDialog.DeleteCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TrackDayAdapter trackDayAdapter;    // Adapter to fill the recyclerview with track days
    private RecyclerView listOfTrackDays;       // Recyclerview that holds the track days
    private int trackDayIdToDelete = 0;         // Used to determine which trackday to delete
    private Location mLastLocation;             // Last location the app received

    private GoogleApiClient mGoogleApiClient;   // GoogleApiClient used for location info

    private static final int TRACK_DAY_LOADER = 1;  // Loader number
    private int REQUEST_PERMISSION_CODE = 8153;     // Code to identify the permission request
    private int LOCATION_SETTINGS_RESOLUTION = 4259;// Code to identify the location resolution request

    // Strings for sending data to Sessions activity
    public static final String TRACK_NAME_DATA = "com.michaelcavalli.trackrider.TRACK_NAME_DATA";
    public static final String DATE_DATA = "com.michaelcavalli.trackrider.DATE_DATA";

    // Projection for data provider
    private static final String[] TRACK_DAY_COLUMNS = {
            DataContract.TrackDays.TABLE_NAME + "." + DataContract.TrackDays._ID,
            DataContract.TrackDays.COLUMN_TRACK_NAME,
            DataContract.TrackDays.COLUMN_TRACK_DAY_DATE
    };

    // Column numbers for data provider
    static final int COL_TRACK_ENTRY_ID = 0;
    static final int COL_TRACK_NAME = 1;
    static final int COL_TRACK_DAY_DATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ad service
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        // Set the ad at the bottom of the screen
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Initialize the GoogleApiClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get reference to the recyclerview and set the layout manager
        listOfTrackDays = (RecyclerView) findViewById(R.id.main_recycler_view);
        listOfTrackDays.setLayoutManager(new LinearLayoutManager(this));

        listOfTrackDays.setHasFixedSize(true);

        // Initialize the adapter for the recyclerview
        trackDayAdapter = new TrackDayAdapter(new TrackDayAdapter.TrackDayOnClickHandler() {

            /**
             * Upon click on a track day, this method determines if it is a short or long click.
             * If it's a long click, it activates the delete dialog to see if the user wants to
             * delete the track day.  If it's a short click, it calls a method to open the session
             * activity.
             * @param vh The viewholder that was clicked
             * @param longClick If it was a long click or not.
             * @return returns true if successful.
             */
            @Override
            public boolean onClick(TrackDayAdapter.TrackDayViewHolder vh, boolean longClick) {

                if (longClick) {
                    trackDayIdToDelete = vh.id;
                    if (trackDayIdToDelete != 0) {
                        DialogFragment newDialog = new DeleteDialog();
                        newDialog.show(getSupportFragmentManager(), getString(R.string.delete_dialog));
                        return true;
                    } else
                        return false; // not successful
                } else {
                    openSessionList(vh.id, vh.trackName.getText().toString(), vh.date.getText().toString());
                    return true;
                }
            }
        });

        // Sets the adapter for the track days recyclerview
        listOfTrackDays.setAdapter(trackDayAdapter);
        // Starts the loader
        getSupportLoaderManager().initLoader(TRACK_DAY_LOADER, null, this);

    }

    /**
     * When the activity starts, we connect to GoogleApiClient
     */
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * When the activity stops, we disconnect from the GoogleApiClient
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Upon connecting to the GoogleApiClient, we attempt to use the Location API
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation = new Location("spoof location");


        // Once connected, we check if we have permission to access coarse location.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            // If we have permission, we attempt to get the latest location from the API
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        else {
            // If we don't have permission, we attempt to get it
            startLocationConnections();
        }
        // If location is not null, get longitude and latitude
        if (mLastLocation != null) {
            Log.v(LOG_TAG, "Latitude is: " + mLastLocation.getLatitude());
            Log.v(LOG_TAG, "Longitude is: " + mLastLocation.getLongitude());
        }
        // If location is null, call method to change permissions and location settings
        else {
            startLocationConnections();
        }
    }

    /**
     * Receiving method on the result of permission request
     *
     * @param requestCode  The code of the request
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            // If the permission request was a success, go ahead and request location updates
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, getLocationRequest(), this);
            }
        }
    }

    /**
     * Receiving method for status resolution
     *
     * @param requestCode Code to identify request
     * @param resultCode  If request was successful or not.
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS_RESOLUTION) {
            if (resultCode == RESULT_OK) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // The location request we want to use
    protected LocationRequest getLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return mLocationRequest;
    }

    /**
     * This method creates a location request, and checks to make sure the settings are acceptable
     * for the request.  If they are, we can request location updates.  If not, we need to request
     * it be changed.
     */
    public void startLocationConnections() {
        // Create the builder for the request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());

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
                        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            // If we don't have permission, we need to request it.
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CODE);
                        } else {
                            // Location settings are good, and we have permission, so lets request updates
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, getLocationRequest(), MainActivity.this);
                        }
                        break;
                    // Location settings are not what we need, attempting to resolve
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show dialog to user by calling startResolutionFoResult on the status
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    LOCATION_SETTINGS_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
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
        Log.v(LOG_TAG, "NEW LONGITUDE: " + mLastLocation.getLongitude());
        Log.v(LOG_TAG, "NEW LATITUDE: " + mLastLocation.getLatitude());
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
     * This method is called by the Delete dialog if the user long clicks on a track day to delete.
     * It will delete the track day along with all associated sessions
     */
    @Override
    public void delete() {
        if (trackDayIdToDelete != 0) {

            // Selection and args to identify sessions to delete
            String selection = DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(trackDayIdToDelete)};

            // Delete all associated sessions first
            getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);

            // Selection for the track days table
            selection = DataContract.TrackDays.TABLE_NAME + "." + DataContract.TrackDays._ID + " = ? ";

            // Delete the track day
            getContentResolver().delete(DataContract.TrackDays.CONTENT_URI, selection, selectionArgs);

            // Notify the adapter that the data has changed
            trackDayAdapter.notifyDataSetChanged();
            trackDayIdToDelete = 0;
        }
    }

    /**
     * This method opens the session list based on which track day was clicked
     * @param id The ID of the track day to pull sessions for
     * @param trackName The track name for the track day
     * @param date The date of the track day
     */
    public void openSessionList(int id, String trackName, String date) {
        Uri uri = DataContract.SessionsEntry.buildSessionsWithTrackDayId(id);
        Intent intent = new Intent(this, SessionsActivity.class);
        intent.setData(uri);
        intent.putExtra(TRACK_NAME_DATA, trackName);
        intent.putExtra(DATE_DATA, date);
        startActivity(intent);
    }

    /**
     * The intent to open the track list screen from the track list button
     * @param view
     */
    public void openTrackList(View view) {
        Intent intent = new Intent(this, TrackListActivity.class);
        startActivity(intent);
    }

    /**
     * This method adds a new track based either on selection from the dialog or from a track
     * location being close based on GPS
     * @param v
     */
    public void addTrackDay(View v) {
        Bundle args = new Bundle();
        DialogFragment addTrackDialog = new AddTrackDayDialog();

        if(trackListEmpty()){
            Toast.makeText(MainActivity.this, R.string.track_list_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // If the last location is not null, check and see if we are close to any tracks
        if (mLastLocation != null) {
            ContentValues closeTrack = checkForCloseTrack();
            // If we are close to a track, use that track for a new track day
            if (closeTrack != null) {
                Uri trackdayUri = DataContract.TrackDays.buildTrackDays();
                getContentResolver().insert(trackdayUri, closeTrack);
            }
            // If we aren't close to a track, use the dialog to choose one
            else
                addTrackDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
        }
        // If we don't have current location info, use the dialog to choose a track
        else
            addTrackDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));


    }

    /**
     * Checks if the track list is empty
     * @return true if empty, false if not
     */
    public boolean trackListEmpty(){
        Cursor trackList = getContentResolver().query(DataContract.TrackEntry.CONTENT_URI, null, null, null, null);
        if(trackList == null || trackList.getCount() == 0)
            return true;
        else
            return false;
    }

    /**
     * This method uses a current location and the GPS location for tracks in the database
     * to check if we are close to a track when  the add track day button is pushed.
     * @return A content values object with the info for a new track day
     */
    public ContentValues checkForCloseTrack() {
        Cursor trackListCursor = getContentResolver().query(DataContract.TrackEntry.CONTENT_URI, null, null, null, null);

        // If there are no tracks, return null
        if (trackListCursor == null)
            return null;

        // Move to the first track in the list
        trackListCursor.moveToFirst();

        // Run through the cursor data and check all GPS information
        for (int i = 0; i < trackListCursor.getCount(); i++) {
            String lo;
            String la;
            Location newLocation = new Location("Track Location");

            // Get the longitude and latitude info from the cursor
            lo = trackListCursor.getString(2);
            la = trackListCursor.getString(3);

            // Make sure the GPS info exists
            if (lo != null && la != null) {

                // Create a location with the GPS info
                newLocation.setLongitude(Double.parseDouble(lo));
                newLocation.setLatitude(Double.parseDouble(la));

                // Calculate the distance between the current location and track location
                float distanceTo = mLastLocation.distanceTo(newLocation);
                // Convert to miles
                double distanceInMiles = distanceTo / 1609.34;

                // If the distance is less than 5 miles, create contentvalues for that track
                if (distanceInMiles < 5) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    String trackDayDate = sdf.format(new Date(System.currentTimeMillis()));

                    ContentValues cv = new ContentValues();
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_NAME, trackListCursor.getString(1));
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_KEY, trackListCursor.getInt(0));
                    cv.put(DataContract.TrackDays.COLUMN_TRACK_DAY_DATE, trackDayDate);

                    // Return the contentvalues for this track
                    return cv;
                }
            }
            // If we didn't find a track yet, move to the next track in the list
            trackListCursor.moveToNext();
        }
        // If we didn't find a close track, return null and pick from dialog
        return null;
    }

    /**
     * On creating the loader, get track day info from the DB
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Make sure we have the right loader
        if (id == TRACK_DAY_LOADER) {
            // Get the URI and sort order for the track day list
            Uri trackListUri = DataContract.TrackDays.buildTrackDays();
            String sortOrder = DataContract.TrackDays.COLUMN_TRACK_DAY_DATE + " ASC";
            // Return a cursor for the track day list
            return new CursorLoader(this, trackListUri, TRACK_DAY_COLUMNS, null, null, sortOrder);
        } else
            return null;
    }

    /**
     * Swap out the cursor for the new one with the track day list info
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        trackDayAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        trackDayAdapter.swapCursor(null);
    }
}
