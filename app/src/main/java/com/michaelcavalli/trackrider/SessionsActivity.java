package com.michaelcavalli.trackrider;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.michaelcavalli.trackrider.data.DataContract;
import com.michaelcavalli.trackrider.dialogs.DeleteDialog;

/**
 * This is the sessions activity that lists all sessions for a chosen track day
 */
public class SessionsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteDialog.DeleteCallback {
    private static final String LOG_TAG = SessionsActivity.class.getSimpleName();

    private static final int SESSION_LIST_LOADER = 3;

    private SessionListAdapter sessionListAdapter;  // The adapter for the recyclerview
    private RecyclerView sessionListRecyclerView;   // The recyclerview that lists all sessions
    private TextView headerTrackName;               // The textview in the header for the track name
    private TextView headerDate;                    // The textview in the header for the date
    private int sessionToDelete;                    // Session to delete if user is deleting
    private int numberOfSessions = 0;               // Number of sessions in the list

    Uri sessionListUri;                             // Uri sent to activity to retrieve sessions
    private String sentTrackName;                   // track name sent from main activity
    private String sentDate;                        // date sent from main activity

    // Projection for loader to call DB with
    private static final String[] SESSION_LIST_COLUMNS = {
            DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID,
            DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER,
            DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY
    };

    // Column numbers to access data in returned cursor
    static final int COL_SESSION_ID = 0;
    static final int COL_SESSION_NUMER = 1;
    static final int COL_TRACK_DAY_KEY = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        // Mobilize ads for this activity
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        // Load ad into adview
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Get the data sent from the main activity for the track day selected
        sessionListUri = getIntent().getData();
        sentTrackName = getIntent().getStringExtra(MainActivity.TRACK_NAME_DATA);
        sentDate = getIntent().getStringExtra(MainActivity.DATE_DATA);

        // Get the recyclerview and
        sessionListRecyclerView = (RecyclerView) findViewById(R.id.sessions_recycler_view);
        sessionListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionListRecyclerView.hasFixedSize();

        // Get the header textviews
        headerTrackName = (TextView) findViewById(R.id.sessions_track_name);
        headerDate = (TextView) findViewById(R.id.sessions_date);

        // Set the header textviews
        headerTrackName.setText(sentTrackName);
        headerDate.setText(sentDate);

        // Create the adapter for the recyclerview
        sessionListAdapter = new SessionListAdapter(this, new SessionListAdapter.SessionListOnClickHandler() {
            // Add an onclick handler to handle session clicks
            @Override
            public boolean onClick(SessionListAdapter.SessionViewHolder vh, boolean longClick) {
                // If longclick then use delete dialog
                if(longClick){
                    sessionToDelete = vh.sessionId;
                    if(sessionToDelete != 0){
                        DialogFragment dialog = new DeleteDialog();
                        dialog.show(getSupportFragmentManager(), getString(R.string.delete_dialog));
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // If short click, use this method to open the detail page for this session
                    openSession(vh.sessionNumber, vh.trackDayKey);
                    return true;
                }
            }
        });

        // Set the adapter on the recyclerview
        sessionListRecyclerView.setAdapter(sessionListAdapter);
        // Start the loader
        getSupportLoaderManager().initLoader(SESSION_LIST_LOADER, null, this);

    }

    /**
     * This method is called if the user chooses to delete the session
     */
    @Override
    public void delete() {
        if(sessionToDelete != 0){
            // Selection to identify session via ID
            String selection = DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID + " = ? ";
            // selection args is the session ID
            String selectionArgs[] = new String[]{Integer.toString(sessionToDelete)};
            // Delete the session in the DB
            getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);
            // Notify the dataset changed
            sessionListAdapter.notifyDataSetChanged();
            // Set the session to delete back to 0
            sessionToDelete = 0;
        }
    }

    /**
     * When the loader is created, query the DB for our track day session, in ASC order based on
     * session number.
     * @param id the ID of the loader
     * @param args the bundle passed
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER + " ASC";
        return new CursorLoader(this,
                sessionListUri,
                SESSION_LIST_COLUMNS,
                null,
                null,
                sortOrder
                );
    }

    /**
     * Once the loader is finished loading, make sure the data is not null, and then swap the cursor
     * in the adapter for the new one.
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null)
            numberOfSessions=data.getCount();
        sessionListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        sessionListAdapter.swapCursor(null);
    }

    /**
     * This method is used to open a detail activity for a selected session
     * @param sessionNumber The session number to open up the detail activity with
     * @param trackDayKey the track day key for this session
     */
    public void openSession(int sessionNumber, int trackDayKey){
        Uri uri = DataContract.SessionsEntry.buildSessionWithTrackDayIdAndSessionNumber(Integer.toString(trackDayKey), Integer.toString(sessionNumber));
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * When the FAB is clicked, this method is called to add a new session to this track day.
     * @param view The view that was clicked
     */
    public void addSession(View view){
        // ContentValues used to add new data to DB
        ContentValues cv = new ContentValues();
        // Increase our session count
        int newSessionNumber = numberOfSessions+1;
        // Get the track day ID from the sent URI and use it to create a new session
        int trackId = Integer.valueOf(DataContract.SessionsEntry.getTrackDayIDFromUri(sessionListUri));

        // Put all data into ContentValues
        cv.put(DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER, newSessionNumber);
        cv.put(DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY, trackId);
        cv.put(DataContract.SessionsEntry.COLUMN_DATE, sentDate);
        cv.put(DataContract.SessionsEntry.COLUMN_TRACK_NAME, sentTrackName);

        // Get URI to use to insert data, and insert
        Uri sessionInsertUri = DataContract.SessionsEntry.CONTENT_URI;
        getContentResolver().insert(sessionInsertUri, cv);

        // Notify the adapter that the dataset has changed
        sessionListAdapter.notifyDataSetChanged();

    }
}
