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
 * Created by silen_000 on 7/28/2016.
 */
public class SessionsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DeleteDialog.DeleteCallback {
    private static final String LOG_TAG = SessionsActivity.class.getSimpleName();

    private static final int SESSION_LIST_LOADER = 3;

    private SessionListAdapter sessionListAdapter;
    private RecyclerView sessionListRecyclerView;
    private TextView headerTrackName;
    private TextView headerDate;
    private int sessionToDelete;
    private int numberOfSessions = 0;

    Uri sessionListUri;
    private String sentTrackName;
    private String sentDate;

    private static final String[] SESSION_LIST_COLUMNS = {
            DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID,
            DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER,
            DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY
    };

    static final int COL_SESSION_ID = 0;
    static final int COL_SESSION_NUMER = 1;
    static final int COL_TRACK_DAY_KEY = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        sessionListUri = getIntent().getData();
        sentTrackName = getIntent().getStringExtra(MainActivity.TRACK_NAME_DATA);
        sentDate = getIntent().getStringExtra(MainActivity.DATE_DATA);

        sessionListRecyclerView = (RecyclerView) findViewById(R.id.sessions_recycler_view);
        sessionListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionListRecyclerView.hasFixedSize();

        headerTrackName = (TextView) findViewById(R.id.sessions_track_name);
        headerDate = (TextView) findViewById(R.id.sessions_date);

        headerTrackName.setText(sentTrackName);
        headerDate.setText(sentDate);

        sessionListAdapter = new SessionListAdapter(this, new SessionListAdapter.SessionListOnClickHandler() {
            @Override
            public boolean onClick(SessionListAdapter.SessionViewHolder vh, boolean longClick) {
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
                    openSession(vh.sessionNumber, vh.trackDayKey);
                    return true;
                }
            }
        });

        sessionListRecyclerView.setAdapter(sessionListAdapter);
        getSupportLoaderManager().initLoader(SESSION_LIST_LOADER, null, this);

    }

    @Override
    public void delete() {
        if(sessionToDelete != 0){
            int rowsDeleted;
            String selection = DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID + " = ? ";
            String selectionArgs[] = new String[]{Integer.toString(sessionToDelete)};
            rowsDeleted = getContentResolver().delete(DataContract.SessionsEntry.CONTENT_URI, selection, selectionArgs);
            Log.v(LOG_TAG, "Session Rows Deleted: " + rowsDeleted);
            sessionListAdapter.notifyDataSetChanged();
            sessionToDelete = 0;
            Log.v(LOG_TAG, "Number of sessions: " + numberOfSessions);
        }
    }

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null)
            numberOfSessions=data.getCount();
        Log.v(LOG_TAG, "NUMBER OF SESSIONS LOADED: " + numberOfSessions);
        sessionListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        sessionListAdapter.swapCursor(null);
    }

    public void openSession(int sessionNumber, int trackDayKey){
        Uri uri = DataContract.SessionsEntry.buildSessionWithTrackDayIdAndSessionNumber(Integer.toString(trackDayKey), Integer.toString(sessionNumber));
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    public void addSession(View view){
        ContentValues cv = new ContentValues();
        int newSessionNumber = numberOfSessions+1;
        Log.v(LOG_TAG, "Ading session number: " + newSessionNumber);
        int trackId = Integer.valueOf(DataContract.SessionsEntry.getTrackDayIDFromUri(sessionListUri));

        cv.put(DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER, newSessionNumber);
        cv.put(DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY, trackId);
        cv.put(DataContract.SessionsEntry.COLUMN_DATE, sentDate);
        cv.put(DataContract.SessionsEntry.COLUMN_TRACK_NAME, sentTrackName);

        Uri sessionInsertUri = DataContract.SessionsEntry.CONTENT_URI;
        Uri resultingInsertUri = getContentResolver().insert(sessionInsertUri, cv);

        Log.v(LOG_TAG, "SUCCESSFULLY INSERTED URI: " + resultingInsertUri + " WITH TRACK DAY ID OF: " + trackId);
        sessionListAdapter.notifyDataSetChanged();

    }
}
