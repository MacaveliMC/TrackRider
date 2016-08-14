package com.michaelcavalli.trackrider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.michaelcavalli.trackrider.data.DataContract;
import com.michaelcavalli.trackrider.data.DataDbHelper;
import com.michaelcavalli.trackrider.dialogs.AddLapTimeDialog;
import com.michaelcavalli.trackrider.dialogs.AddTrackDialog;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by silen_000 on 8/2/2016.
 */
public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DetailsTextWatcher.TextWatcherCallback, AddLapTimeDialog.ReturnLapTimeInterface {

    private TextView headerTrackName;
    private TextView headerDate;
    private TextView headerSessionNumber;
    private Uri sentUri;
    ArrayAdapter<String> lapTimesListAdapter;
    private int sessionId;


    private Cursor detailsCursor;

    private static final int DETAILS_LOADER_ID = 4;

    private static final String[] DETAIL_COLUMNS = new String[]{
            DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID,
            DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY,
            DataContract.SessionsEntry.COLUMN_TRACK_NAME,
            DataContract.SessionsEntry.COLUMN_DATE,
            DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER,
            DataContract.SessionsEntry.COLUMN_FRONT_COMPRESSION,
            DataContract.SessionsEntry.COLUMN_FRONT_REBOUND,
            DataContract.SessionsEntry.COLUMN_FRONT_PRELOAD,
            DataContract.SessionsEntry.COLUMN_FORK_HEIGHT,
            DataContract.SessionsEntry.COLUMN_OIL_HEIGHT,
            DataContract.SessionsEntry.COLUMN_FRONT_SPRING_RATE,
            DataContract.SessionsEntry.COLUMN_REAR_COMPRESSION,
            DataContract.SessionsEntry.COLUMN_REAR_REBOUND,
            DataContract.SessionsEntry.COLUMN_REAR_PRELOAD,
            DataContract.SessionsEntry.COLUMN_REAR_HEIGHT,
            DataContract.SessionsEntry.COLUMN_REAR_SPRING_RATE,
            DataContract.SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_OUT,
            DataContract.SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_IN,
            DataContract.SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_OUT,
            DataContract.SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_IN,
            DataContract.SessionsEntry.COLUMN_FRONT_SPROCKET,
            DataContract.SessionsEntry.COLUMN_REAR_SPROCKET,
            DataContract.SessionsEntry.COLUMN_LAP_TIMES
    };

    private static int COL_SESSION_ID = 0;
    private static int COL_TRACK_DAY_KEY = 1;
    private static int COL_TRACK_NAME = 2;
    private static int COL_DATE = 3;
    private static int COL_SESSION_NUMBER = 4;
    private static int COL_FRONT_COMPRESSION = 5;
    private static int COL_FRONT_REBOUND = 6;
    private static int COL_FRONT_PRELOAD = 7;
    private static int COL_FORK_HEIGHT = 8;
    private static int COL_OIL_HEIGHT = 9;
    private static int COL_FRONT_SPRING_RATE = 10;
    private static int COL_REAR_COMPRESSION = 11;
    private static int COL_REAR_REBOUND = 12;
    private static int COL_REAR_PRELOAD = 13;
    private static int COL_REAR_HEIGHT = 14;
    private static int COL_REAR_SPRING_RATE = 15;
    private static int COL_FRONT_TIRE_PRESSURE_OUT = 16;
    private static int COL_FRONT_TIRE_PRESSURE_IN = 17;
    private static int COL_REAR_TIRE_PRESSURE_OUT = 18;
    private static int COL_REAR_TIRE_PRESSURE_IN = 19;
    private static int COL_FRONT_SPROCKET = 20;
    private static int COL_REAR_SPROCKET = 21;
    private static int COL_LAP_TIMES = 22;

    LinearLayout lapTimesLinearLayout;
    String tempLapTimeList;
    String[] lapTimeStringList;
    Button add_lap_time;

    TextView frontCompressionTitle;
    TextView frontReboundTitle;
    TextView frontPreloadTitle;
    TextView forkHeightTitle;
    TextView oilHeightTitle;
    TextView frontSpringRateTitle;
    TextView rearCompressionTitle;
    TextView rearReboundTitle;
    TextView rearPreloadTitle;
    TextView rearHeightTitle;
    TextView rearSpringRateTitle;
    EditText frontCompressionValue;
    EditText frontReboundValue;
    EditText frontPreloadValue;
    EditText forkHeightValue;
    EditText oilHeightValue;
    EditText frontSpringRateValue;
    EditText rearCompressionValue;
    EditText rearReboundValue;
    EditText rearPreloadValue;
    EditText rearHeightValue;
    EditText rearSpringRateValue;

    TextView frontTirePressureOutTitle;
    TextView frontTirePressureInTitle;
    TextView rearTirePressureOutTitle;
    TextView rearTirePressureInTitle;
    EditText frontTirePressureOutValue;
    EditText frontTirePressureInValue;
    EditText rearTirePressureOutValue;
    EditText rearTirePressureInValue;

    TextView frontSprocketTitle;
    TextView rearSprocketTitle;
    EditText frontSprocketValue;
    EditText rearSprocketValue;

    ImageView lapTimes;
    ImageView suspension;
    ImageView tires;
    ImageView gearing;

    boolean lapTimesExpanded = false;
    boolean suspensionExpanded = false;
    boolean tiresExpanded = false;
    boolean gearingExpanded = false;


    String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        lapTimesLinearLayout = (LinearLayout) findViewById(R.id.lap_times_linearlayout);

        headerTrackName = (TextView) findViewById(R.id.header_track_name);
        headerDate = (TextView) findViewById(R.id.header_date);
        headerSessionNumber = (TextView) findViewById(R.id.session_number_header);

        add_lap_time = (Button) findViewById(R.id.add_lap_time_button);

        sentUri = getIntent().getData();


        frontCompressionTitle = (TextView) findViewById(R.id.frontCompressionTitle);
        frontReboundTitle = (TextView) findViewById(R.id.frontReboundTitle);
        frontPreloadTitle = (TextView) findViewById(R.id.frontPreloadTitle);
        forkHeightTitle = (TextView) findViewById(R.id.forkHeightTitle);
        oilHeightTitle = (TextView) findViewById(R.id.oilHeightTitle);
        frontSpringRateTitle = (TextView) findViewById(R.id.frontSpringRateTitle);
        rearCompressionTitle = (TextView) findViewById(R.id.rearCompressionTitle);
        rearReboundTitle = (TextView) findViewById(R.id.rearReboundTitle);
        rearPreloadTitle = (TextView) findViewById(R.id.rearPreloadTitle);
        rearHeightTitle = (TextView) findViewById(R.id.rearHeightTitle);
        rearSpringRateTitle = (TextView) findViewById(R.id.rearSpringRateTitle);
        frontCompressionValue = (EditText) findViewById(R.id.frontCompressionValue);
        frontReboundValue = (EditText) findViewById(R.id.frontReboundValue);
        frontPreloadValue = (EditText) findViewById(R.id.frontPreloadValue);
        forkHeightValue = (EditText) findViewById(R.id.forkHeightValue);
        oilHeightValue = (EditText) findViewById(R.id.oilHeightValue);
        frontSpringRateValue = (EditText) findViewById(R.id.frontSpringRateValue);
        rearCompressionValue = (EditText) findViewById(R.id.rearCompressionValue);
        rearReboundValue = (EditText) findViewById(R.id.rearReboundValue);
        rearPreloadValue = (EditText) findViewById(R.id.rearPreloadValue);
        rearHeightValue = (EditText) findViewById(R.id.rearHeightValue);
        rearSpringRateValue = (EditText) findViewById(R.id.rearSpringRateValue);

        frontTirePressureOutTitle = (TextView) findViewById(R.id.frontTirePressureOutTitle);
        frontTirePressureInTitle = (TextView) findViewById(R.id.frontTirePressureInTitle);
        rearTirePressureOutTitle = (TextView) findViewById(R.id.rearTirePressureOutTitle);
        rearTirePressureInTitle = (TextView) findViewById(R.id.rearTirePressureInTitle);
        frontTirePressureOutValue = (EditText) findViewById(R.id.frontTirePressureOutValue);
        frontTirePressureInValue = (EditText) findViewById(R.id.frontTirePressureInValue);
        rearTirePressureOutValue = (EditText) findViewById(R.id.rearTirePressureOutValue);
        rearTirePressureInValue = (EditText) findViewById(R.id.rearTirePressureInValue);

        frontSprocketTitle = (TextView) findViewById(R.id.frontSprocketTitle);
        rearSprocketTitle = (TextView) findViewById(R.id.rearSprocketTitle);
        frontSprocketValue = (EditText) findViewById(R.id.frontSprocketValue);
        rearSprocketValue = (EditText) findViewById(R.id.rearSprocketValue);

        lapTimes = (ImageView) findViewById(R.id.lapTimes);
        lapTimes.setContentDescription(getString(R.string.click_to_expand));
        suspension = (ImageView) findViewById(R.id.suspension);
        suspension.setContentDescription(getString(R.string.click_to_expand));
        tires = (ImageView) findViewById(R.id.tires);
        tires.setContentDescription(getString(R.string.click_to_expand));
        gearing = (ImageView) findViewById(R.id.gearing);
        gearing.setContentDescription(getString(R.string.click_to_expand));

        Picasso.with(this).load(R.drawable.add_button).into(lapTimes);
        Picasso.with(this).load(R.drawable.add_button).into(suspension);
        Picasso.with(this).load(R.drawable.add_button).into(tires);
        Picasso.with(this).load(R.drawable.add_button).into(gearing);

        lapTimesLinearLayout.setVisibility(View.GONE);
        add_lap_time.setVisibility(View.GONE);

        frontCompressionTitle.setVisibility(View.GONE);
        frontReboundTitle.setVisibility(View.GONE);
        frontPreloadTitle.setVisibility(View.GONE);
        forkHeightTitle.setVisibility(View.GONE);
        oilHeightTitle.setVisibility(View.GONE);
        frontSpringRateTitle.setVisibility(View.GONE);
        rearCompressionTitle.setVisibility(View.GONE);
        rearReboundTitle.setVisibility(View.GONE);
        rearPreloadTitle.setVisibility(View.GONE);
        rearHeightTitle.setVisibility(View.GONE);
        rearSpringRateTitle.setVisibility(View.GONE);
        frontCompressionValue.setVisibility(View.GONE);
        frontReboundValue.setVisibility(View.GONE);
        frontPreloadValue.setVisibility(View.GONE);
        forkHeightValue.setVisibility(View.GONE);
        oilHeightValue.setVisibility(View.GONE);
        frontSpringRateValue.setVisibility(View.GONE);
        rearCompressionValue.setVisibility(View.GONE);
        rearReboundValue.setVisibility(View.GONE);
        rearPreloadValue.setVisibility(View.GONE);
        rearHeightValue.setVisibility(View.GONE);
        rearSpringRateValue.setVisibility(View.GONE);

        frontTirePressureOutTitle.setVisibility(View.GONE);
        frontTirePressureInTitle.setVisibility(View.GONE);
        rearTirePressureOutTitle.setVisibility(View.GONE);
        rearTirePressureInTitle.setVisibility(View.GONE);
        frontTirePressureOutValue.setVisibility(View.GONE);
        frontTirePressureInValue.setVisibility(View.GONE);
        rearTirePressureOutValue.setVisibility(View.GONE);
        rearTirePressureInValue.setVisibility(View.GONE);

        frontSprocketTitle.setVisibility(View.GONE);
        frontSprocketValue.setVisibility(View.GONE);
        rearSprocketTitle.setVisibility(View.GONE);
        rearSprocketValue.setVisibility(View.GONE);

        getSupportLoaderManager().initLoader(4, null, this);

        EditText[] editTextGroup = new EditText[]{
                frontCompressionValue,
                frontReboundValue,
                frontPreloadValue,
                forkHeightValue,
                oilHeightValue,
                frontSpringRateValue,
                rearCompressionValue,
                rearReboundValue,
                rearPreloadValue,
                rearHeightValue,
                rearSpringRateValue,
                frontTirePressureOutValue,
                frontTirePressureInValue,
                rearTirePressureOutValue,
                rearTirePressureInValue,
                frontSprocketValue,
                rearSprocketValue
        };

        setListeners(editTextGroup);

    }

    public void expandButton(View view) {
        Log.v(LOG_TAG, "CLICKED EXPAND BUTTON");
        int visibility;

        if (view.getId() == lapTimes.getId()) {
            visibility = expandCollapseImageChangeMethod(lapTimesExpanded, lapTimes);

            if (visibility == View.GONE)
                lapTimesExpanded = false;
            else
                lapTimesExpanded = true;

            add_lap_time.setVisibility(visibility);
            lapTimesLinearLayout.setVisibility(visibility);

        }

        if (view.getId() == suspension.getId()) {
            visibility = expandCollapseImageChangeMethod(suspensionExpanded, suspension);

            if (visibility == View.GONE)
                suspensionExpanded = false;
            else
                suspensionExpanded = true;

            frontCompressionTitle.setVisibility(visibility);
            frontReboundTitle.setVisibility(visibility);
            frontPreloadTitle.setVisibility(visibility);
            forkHeightTitle.setVisibility(visibility);
            oilHeightTitle.setVisibility(visibility);
            frontSpringRateTitle.setVisibility(visibility);
            rearCompressionTitle.setVisibility(visibility);
            rearReboundTitle.setVisibility(visibility);
            rearPreloadTitle.setVisibility(visibility);
            rearHeightTitle.setVisibility(visibility);
            rearSpringRateTitle.setVisibility(visibility);
            frontCompressionValue.setVisibility(visibility);
            frontReboundValue.setVisibility(visibility);
            frontPreloadValue.setVisibility(visibility);
            forkHeightValue.setVisibility(visibility);
            oilHeightValue.setVisibility(visibility);
            frontSpringRateValue.setVisibility(visibility);
            rearCompressionValue.setVisibility(visibility);
            rearReboundValue.setVisibility(visibility);
            rearPreloadValue.setVisibility(visibility);
            rearHeightValue.setVisibility(visibility);
            rearSpringRateValue.setVisibility(visibility);

        }

        if (view.getId() == tires.getId()) {
            visibility = expandCollapseImageChangeMethod(tiresExpanded, tires);

            if (visibility == View.GONE)
                tiresExpanded = false;
            else
                tiresExpanded = true;

            frontTirePressureOutTitle.setVisibility(visibility);
            frontTirePressureInTitle.setVisibility(visibility);
            rearTirePressureOutTitle.setVisibility(visibility);
            rearTirePressureInTitle.setVisibility(visibility);
            frontTirePressureOutValue.setVisibility(visibility);
            frontTirePressureInValue.setVisibility(visibility);
            rearTirePressureOutValue.setVisibility(visibility);
            rearTirePressureInValue.setVisibility(visibility);

        }

        if (view.getId() == gearing.getId()) {
            visibility = expandCollapseImageChangeMethod(gearingExpanded, gearing);

            if (visibility == View.GONE)
                gearingExpanded = false;
            else
                gearingExpanded = true;

            frontSprocketTitle.setVisibility(visibility);
            frontSprocketValue.setVisibility(visibility);
            rearSprocketTitle.setVisibility(visibility);
            rearSprocketValue.setVisibility(visibility);

        }

    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private int expandCollapseImageChangeMethod(boolean expanded, View view) {
        ImageView v = (ImageView) view;
        if (expanded) {
            v.setContentDescription(getString(R.string.click_to_expand));
            Picasso.with(this).load(R.drawable.add_button).into(v);
            hideKeyboard(view); // Hide keyboard if it's open
            return View.GONE;
        } else {
            v.setContentDescription(getString(R.string.click_to_collapse));
            Picasso.with(this).load(R.drawable.minus_button).into(v);
            return View.VISIBLE;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, sentUri, DETAIL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() == 1) {

            data.moveToFirst();


            headerTrackName.setText(data.getString(COL_TRACK_NAME));
            headerTrackName.setContentDescription("Track name is " + data.getString(COL_TRACK_NAME));
            headerDate.setText(data.getString(COL_DATE));
            headerDate.setContentDescription("Date of session is " + data.getString(COL_DATE));
            headerSessionNumber.setText("Session " + data.getString(COL_SESSION_NUMBER));
            headerSessionNumber.setContentDescription("Session number is " + data.getString(COL_SESSION_NUMBER));
            sessionId = data.getInt(COL_SESSION_ID);
            tempLapTimeList = data.getString(COL_LAP_TIMES);
            Log.v(LOG_TAG, "temp list: " + tempLapTimeList);
            if (tempLapTimeList != null) {
                lapTimeStringList = tempLapTimeList.split(";");
                for(int i=0; i<lapTimeStringList.length; i++)
                    Log.v(LOG_TAG, "LAP TIME FROM DATABASE: " + lapTimeStringList[i]);
                addLapTimeTextViewsFromData(lapTimeStringList);
            }

            frontCompressionValue.setText(data.getString(COL_FRONT_COMPRESSION));
            frontReboundValue.setText(data.getString(COL_FRONT_REBOUND));
            frontPreloadValue.setText(data.getString(COL_FRONT_PRELOAD));
            forkHeightValue.setText(data.getString(COL_FORK_HEIGHT));
            oilHeightValue.setText(data.getString(COL_OIL_HEIGHT));
            frontSpringRateValue.setText(data.getString(COL_FRONT_SPRING_RATE));
            rearCompressionValue.setText(data.getString(COL_REAR_COMPRESSION));
            rearReboundValue.setText(data.getString(COL_REAR_REBOUND));
            rearPreloadValue.setText(data.getString(COL_REAR_PRELOAD));
            rearHeightValue.setText(data.getString(COL_REAR_HEIGHT));
            rearSpringRateValue.setText(data.getString(COL_REAR_SPRING_RATE));

            frontTirePressureOutValue.setText(data.getString(COL_FRONT_TIRE_PRESSURE_OUT));
            frontTirePressureInValue.setText(data.getString(COL_FRONT_TIRE_PRESSURE_IN));
            rearTirePressureOutValue.setText(data.getString(COL_REAR_TIRE_PRESSURE_OUT));
            rearTirePressureInValue.setText(data.getString(COL_REAR_TIRE_PRESSURE_IN));

            frontSprocketValue.setText(data.getString(COL_FRONT_SPROCKET));
            rearSprocketValue.setText(data.getString(COL_REAR_SPROCKET));

        } else
            Log.v(LOG_TAG, "DATA COULD BE NULL, COUNT IS: " + data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public void setListeners(EditText[] group) {
        for (int i = 0; i < group.length; i++) {
            if (group[i] == null)
                Log.v(LOG_TAG, "OBJECT EQUAL TO NULL!!!");
            else
                group[i].addTextChangedListener(new DetailsTextWatcher(group[i], this));
        }
    }

    @Override
    public void RecordData(EditText et) {
        ContentValues cv = new ContentValues();


        if (et == frontCompressionValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_COMPRESSION, et.getText().toString());
        if (et == frontReboundValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_REBOUND, et.getText().toString());
        if (et == frontPreloadValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_PRELOAD, et.getText().toString());
        if (et == forkHeightValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FORK_HEIGHT, et.getText().toString());
        if (et == oilHeightValue)
            cv.put(DataContract.SessionsEntry.COLUMN_OIL_HEIGHT, et.getText().toString());
        if (et == frontSpringRateValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_SPRING_RATE, et.getText().toString());
        if (et == rearCompressionValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_COMPRESSION, et.getText().toString());
        if (et == rearReboundValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_REBOUND, et.getText().toString());
        if (et == rearPreloadValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_PRELOAD, et.getText().toString());
        if (et == rearHeightValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_HEIGHT, et.getText().toString());
        if (et == rearSpringRateValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_SPRING_RATE, et.getText().toString());

        if (et == frontTirePressureOutValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_OUT, et.getText().toString());
        if (et == frontTirePressureInValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_IN, et.getText().toString());
        if (et == rearTirePressureOutValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_OUT, et.getText().toString());
        if (et == rearTirePressureInValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_IN, et.getText().toString());

        if (et == frontSprocketValue)
            cv.put(DataContract.SessionsEntry.COLUMN_FRONT_SPROCKET, et.getText().toString());
        if (et == rearSprocketValue)
            cv.put(DataContract.SessionsEntry.COLUMN_REAR_SPROCKET, et.getText().toString());

        int rowsUpdated = insertNewSessionData(cv);

        Log.v(LOG_TAG, "ROW UPDATED: " + rowsUpdated);

    }

    public void addLapTime(View view) {
        DialogFragment newDialog = new AddLapTimeDialog();
        newDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
    }

    @Override
    public void OnDialogPositiveClick(DialogFragment dialog) {
        String lapTimeEntered;
        EditText lap_time_text = (EditText) dialog.getDialog().findViewById(R.id.lap_time_entry);
        lapTimeEntered = lap_time_text.getText().toString();
        Log.v(LOG_TAG, "LAP TIME ENTERED: " + lapTimeEntered);
        Log.v(LOG_TAG, "OLD STRING LIST: ");
        if (lapTimeStringList == null)
            Log.v(LOG_TAG, "EQUAL TO NULL!!!!!!!!!!!!!!!!!!!!!!!");
        else {
            for (int i = 0; i < lapTimeStringList.length; i++)
                Log.v(LOG_TAG, "Lap " + i + ": " + lapTimeStringList[i]);
        }
        lapTimeStringList = AddLapTimeToList(lapTimeEntered, lapTimeStringList);
        Log.v(LOG_TAG, "NEW STRING LIST: ");
        for (int i = 0; i < lapTimeStringList.length; i++)
            Log.v(LOG_TAG, "Lap " + i + ": " + lapTimeStringList[i]);
    }

    @Override
    public void OnDialogNegativeClick(DialogFragment dialog) {

    }

    public String[] AddLapTimeToList(String lap_time_to_add, String[] currentStringList) {
        String[] newStringList;
        ContentValues cv = new ContentValues();
        String finalString;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tv = new TextView(this);
        tv.setLayoutParams(params);
        if (currentStringList != null)
            tv.setText("Lap " + (currentStringList.length + 1) + "- " + lap_time_to_add);
        else
            tv.setText("Lap 1 -  " + lap_time_to_add);
        lapTimesLinearLayout.addView(tv);


        if (currentStringList == null) {
            newStringList = new String[]{lap_time_to_add};
        } else {
            newStringList = new String[(currentStringList.length + 1)];
            for (int i = 0; i <= currentStringList.length; i++)
                if (i < currentStringList.length) {
                    newStringList[i] = currentStringList[i];
                } else {
                    newStringList[i] = lap_time_to_add;
                }
        }

        finalString = ConvertStringListToString(newStringList);
        Log.v(LOG_TAG, "FINAL STRING LIST: " + finalString);

        cv.put(DataContract.SessionsEntry.COLUMN_LAP_TIMES, finalString);

        int rowsUpdated = insertNewSessionData(cv);

        Log.v(LOG_TAG, "ROWS UPDATED WITH LAP TIMES: " + rowsUpdated);

        return newStringList;
    }

    public void addLapTimeTextViewsFromData(String[] lapTimeInfo) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        for (int i = 0; i < lapTimeInfo.length; i++) {
            TextView tv = new TextView(this);
            tv.setTextSize(20);
            tv.setLayoutParams(params);
            tv.setText("Lap " + (i + 1) + " - " + lapTimeInfo[i]);
            tv.setContentDescription("Lap time is " + lapTimeInfo[i]);
            tv.setFocusable(true);
            lapTimesLinearLayout.addView(tv);
        }
    }

    public String ConvertStringListToString(String[] convertList) {
        String finalString = convertList[0] + ";";

        if (convertList != null)
            for (int i = 1; i < convertList.length; i++)
                finalString = finalString + (convertList[i] + ";");

        return finalString;
    }

    public int insertNewSessionData(ContentValues values) {
        String selection = DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID + " = ?";
        String selectionArgs[] = new String[]{Integer.toString(sessionId)};
        Uri updateUri = DataContract.SessionsEntry.CONTENT_URI;

        int rowUpdated = getContentResolver().update(updateUri, values, selection, selectionArgs);

        return rowUpdated;
    }


}
