package com.michaelcavalli.trackrider.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.michaelcavalli.trackrider.R;
import com.michaelcavalli.trackrider.data.DataContract;

import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Dialog for adding a new track day
 */
public class AddTrackDayDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = AddTrackDialog.class.getSimpleName();

    // Cursor Adapter to fill list of tracks in dialog
    private SimpleCursorAdapter sca;
    // Cursor for adapter to use that points to track list information
    Cursor tracklistCursor;

    // Projection to return from data provider
    private static final String[] TRACK_LIST_COLIMNS = {
            DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry._ID,
            DataContract.TrackEntry.COLUMN_TRACK_NAME
    };

    // Column for adapter to take information from
    private static final String[] from = {
            DataContract.TrackDays.COLUMN_TRACK_NAME
    };

    // TextView in layout to map column information to
    int toList[] = new int[] {
            R.id.choose_track_name
    };

    private static final int COL_TRACK_ID = 0;
    private static final int COL_TRACK_NAME = 1;

    private static final int LOADER_NUMBER = 3;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Create adapter for track list
        sca = new SimpleCursorAdapter(getContext(), R.layout.add_track_day_dialog_list_item, tracklistCursor, from, toList, 2);
        // Start loader to load data for cursor adapter
        getActivity().getSupportLoaderManager().initLoader(LOADER_NUMBER, null, this);


        builder.setTitle(R.string.pick_track)
                // Set the adapter for this dialog
                .setAdapter(sca, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Move the cursor to the clicked item
                        tracklistCursor.moveToPosition(i);

                        // Get the track name of the clicked item
                        String trackDayTrack = tracklistCursor.getString(COL_TRACK_NAME);
                        // Get the track id of the clicked item
                        int trackKey = tracklistCursor.getInt(COL_TRACK_ID);

                        // Create the date format and new date using the current time
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        String trackDayDate = sdf.format(new Date(System.currentTimeMillis()));

                        // Create contentvalues variable to put new values in
                        ContentValues cv = new ContentValues();

                        // Add new track day values
                        cv.put(DataContract.TrackDays.COLUMN_TRACK_NAME, trackDayTrack);
                        cv.put(DataContract.TrackDays.COLUMN_TRACK_KEY, trackKey);
                        cv.put(DataContract.TrackDays.COLUMN_TRACK_DAY_DATE, trackDayDate);

                        // Add the new track day
                        Uri trackdayUri = DataContract.TrackDays.buildTrackDays();
                        getActivity().getContentResolver().insert(trackdayUri, cv);
                    }
                });

        return builder.create();
    }

    /**
     * @param id the ID of the loader
     * @param args any arguments passed
     * @return a cursor pointing to the track list data returned
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                DataContract.TrackEntry.buildTrackListUri(),
                TRACK_LIST_COLIMNS,
                null,
                null,
                null
        );
    }

    /**
     * Swap the cursor for the track list with the new one, update the cursor in the adapter
     * @param loader the loader that called this method
     * @param data the cursor returned, either null or with data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        tracklistCursor=data;
        sca.swapCursor(tracklistCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Not used
    }
}
