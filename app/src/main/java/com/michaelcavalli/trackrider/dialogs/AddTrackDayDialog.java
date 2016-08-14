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
 * Created by silen_000 on 8/7/2016.
 */
public class AddTrackDayDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = AddTrackDialog.class.getSimpleName();

    CharSequence[] trackNamesList=null;
    private int checkedId=-1;
    String chosenTrack;
    private SimpleCursorAdapter sca;
    Cursor tracklistCursor;

    private String longitude;
    private String latitude;
    private Location currentLocation;

    public final static String LONGITUDE = "com.michcaelcavalli.trackrider.LONGITUDE";
    public final static String LATITUDE = "com.michaelcavalli.trackrider.LATITUDE";

    CharSequence[] secondList = new CharSequence[] {
            "GRATTAN",
            "GINGERMAN",
            "SOMEWHRE",
            "SOMEWHERE ELSE"
    };

    private static final String[] TRACK_LIST_COLIMNS = {
            DataContract.TrackEntry.TABLE_NAME + "." + DataContract.TrackEntry._ID,
            DataContract.TrackEntry.COLUMN_TRACK_NAME,
            DataContract.TrackEntry.COLUMN_LATITUDE,
            DataContract.TrackEntry.COLUMN_LONGITUDE
    };

    private static final String[] from = {
            DataContract.TrackDays.COLUMN_TRACK_NAME
    };

    int toList[] = new int[] {
            R.id.choose_track_name
    };

    private static final int COL_TRACK_ID = 0;
    private static final int COL_TRACK_NAME = 1;
    private static final int COL_TRACK_LATITUDE = 2;
    private static final int COL_TRACK_LONGITUDE = 3;

    private static final int LOADER_NUMBER = 3;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        sca = new SimpleCursorAdapter(getContext(), R.layout.add_track_day_dialog_list_item, tracklistCursor, from, toList, 2);
        getActivity().getSupportLoaderManager().initLoader(LOADER_NUMBER, null, this);

        Bundle sentBundle = getArguments();



        if(sentBundle != null){
            latitude = sentBundle.getString(LATITUDE);
            longitude = sentBundle.getString(LONGITUDE);
        }


        builder.setTitle(R.string.pick_track)

                .setAdapter(sca, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tracklistCursor.moveToPosition(i);

                        String trackDayTrack = tracklistCursor.getString(COL_TRACK_NAME);
                        int trackKey = tracklistCursor.getInt(COL_TRACK_ID);
                        Log.v(LOG_TAG, "ADDING TRACK DAY WITH TRACK KEY: " + trackKey);

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        String trackDayDate = sdf.format(new Date(System.currentTimeMillis()));

                        ContentValues cv = new ContentValues();

                        cv.put(DataContract.TrackDays.COLUMN_TRACK_NAME, trackDayTrack);
                        cv.put(DataContract.TrackDays.COLUMN_TRACK_KEY, trackKey);
                        cv.put(DataContract.TrackDays.COLUMN_TRACK_DAY_DATE, trackDayDate);

                        //Add the new track day!
                        Uri trackdayUri = DataContract.TrackDays.buildTrackDays();
                        Uri insertUri = getActivity().getContentResolver().insert(trackdayUri, cv);


                    }
                });

        return builder.create();
    }

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        tracklistCursor=data;
        sca.swapCursor(tracklistCursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
