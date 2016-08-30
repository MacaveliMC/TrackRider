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
 * This activity shows the details of the chosen session
 */
public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DetailsTextWatcher.TextWatcherCallback, AddLapTimeDialog.ReturnLapTimeInterface {
    String LOG_TAG = DetailsActivity.class.getSimpleName();

    private TextView headerTrackName;           // The header textview for the track name
    private TextView headerDate;                // The header textview for the date
    private TextView headerSessionNumber;       // The header textview for the session number
    private Uri sentUri;                        // The Uri sent from the previous activity for this session
    private int sessionId;                      // The ID of the current session

    // ID for this loader
    private static final int DETAILS_LOADER_ID = 4;

    // Projection to return from data provider
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

    // Column number rererences for returned data, based on projection
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

    LinearLayout lapTimesLinearLayout;  // Linear layout for lap times
    String tempLapTimeList;             // String of lap times retrieved from data provider
    String[] lapTimeStringList;         // String array of lap times
    Button add_lap_time;                // Button to add lap time to data

    // TextViews & EditTexts suspension info
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

    // Textviews & EditTexts for tire info
    TextView frontTirePressureOutTitle;
    TextView frontTirePressureInTitle;
    TextView rearTirePressureOutTitle;
    TextView rearTirePressureInTitle;
    EditText frontTirePressureOutValue;
    EditText frontTirePressureInValue;
    EditText rearTirePressureOutValue;
    EditText rearTirePressureInValue;

    // Textviews & Edittexts for gearing info
    TextView frontSprocketTitle;
    TextView rearSprocketTitle;
    EditText frontSprocketValue;
    EditText rearSprocketValue;

    // ImageViews for expand/collapse images
    ImageView lapTimes;
    ImageView suspension;
    ImageView tires;
    ImageView gearing;

    // All sections start out collapsed
    boolean lapTimesExpanded = false;
    boolean suspensionExpanded = false;
    boolean tiresExpanded = false;
    boolean gearingExpanded = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Linear layout the lap time textviews are added to
        lapTimesLinearLayout = (LinearLayout) findViewById(R.id.lap_times_linearlayout);

        // Header information textviews
        headerTrackName = (TextView) findViewById(R.id.header_track_name);
        headerDate = (TextView) findViewById(R.id.header_date);
        headerSessionNumber = (TextView) findViewById(R.id.session_number_header);

        // Button to add lap times
        add_lap_time = (Button) findViewById(R.id.add_lap_time_button);

        // Uri sent from session list activity for this session
        sentUri = getIntent().getData();

        // Get references for all suspension textviews and edittexts
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

        // Get references for all tire textviews and edittexts
        frontTirePressureOutTitle = (TextView) findViewById(R.id.frontTirePressureOutTitle);
        frontTirePressureInTitle = (TextView) findViewById(R.id.frontTirePressureInTitle);
        rearTirePressureOutTitle = (TextView) findViewById(R.id.rearTirePressureOutTitle);
        rearTirePressureInTitle = (TextView) findViewById(R.id.rearTirePressureInTitle);
        frontTirePressureOutValue = (EditText) findViewById(R.id.frontTirePressureOutValue);
        frontTirePressureInValue = (EditText) findViewById(R.id.frontTirePressureInValue);
        rearTirePressureOutValue = (EditText) findViewById(R.id.rearTirePressureOutValue);
        rearTirePressureInValue = (EditText) findViewById(R.id.rearTirePressureInValue);

        // Get references for all gearing textviews and edittexts
        frontSprocketTitle = (TextView) findViewById(R.id.frontSprocketTitle);
        rearSprocketTitle = (TextView) findViewById(R.id.rearSprocketTitle);
        frontSprocketValue = (EditText) findViewById(R.id.frontSprocketValue);
        rearSprocketValue = (EditText) findViewById(R.id.rearSprocketValue);

        // Get references for all expand/collapse imageviews
        lapTimes = (ImageView) findViewById(R.id.lapTimes);
        lapTimes.setContentDescription(getString(R.string.click_to_expand));
        suspension = (ImageView) findViewById(R.id.suspension);
        suspension.setContentDescription(getString(R.string.click_to_expand));
        tires = (ImageView) findViewById(R.id.tires);
        tires.setContentDescription(getString(R.string.click_to_expand));
        gearing = (ImageView) findViewById(R.id.gearing);
        gearing.setContentDescription(getString(R.string.click_to_expand));

        // Load images into imageviews
        Picasso.with(this).load(R.drawable.add_button).into(lapTimes);
        Picasso.with(this).load(R.drawable.add_button).into(suspension);
        Picasso.with(this).load(R.drawable.add_button).into(tires);
        Picasso.with(this).load(R.drawable.add_button).into(gearing);

        // Set laptimes linearlayout & button to be gone at first
        lapTimesLinearLayout.setVisibility(View.GONE);
        add_lap_time.setVisibility(View.GONE);

        // Set all suspension textviews and edittexts to gone at first
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

        // Set all tire textviews and edittexts to gone at first
        frontTirePressureOutTitle.setVisibility(View.GONE);
        frontTirePressureInTitle.setVisibility(View.GONE);
        rearTirePressureOutTitle.setVisibility(View.GONE);
        rearTirePressureInTitle.setVisibility(View.GONE);
        frontTirePressureOutValue.setVisibility(View.GONE);
        frontTirePressureInValue.setVisibility(View.GONE);
        rearTirePressureOutValue.setVisibility(View.GONE);
        rearTirePressureInValue.setVisibility(View.GONE);

        // Set all gearing textviews and edittexts to gone at first
        frontSprocketTitle.setVisibility(View.GONE);
        frontSprocketValue.setVisibility(View.GONE);
        rearSprocketTitle.setVisibility(View.GONE);
        rearSprocketValue.setVisibility(View.GONE);

        // Start loader to load info
        getSupportLoaderManager().initLoader(DETAILS_LOADER_ID, null, this);

        // Array of all the edittexts
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

        // Set listeners for changing text on all the edittexts
        setListeners(editTextGroup);

    }

    /**
     * Expands or collapses the section of the clicked button
     * @param view
     */
    public void expandButton(View view) {
        int visibility;

        // Button for lap time section
        if (view.getId() == lapTimes.getId()) {
            // Expand or collapse the section, and get the new visibility
            visibility = expandCollapseImageChangeMethod(lapTimesExpanded, lapTimes);

            // Determine if items in section should be visible or gone now
            if (visibility == View.GONE)
                lapTimesExpanded = false;
            else
                lapTimesExpanded = true;

            // Set the new visibilities
            add_lap_time.setVisibility(visibility);
            lapTimesLinearLayout.setVisibility(visibility);

        }

        // Button for suspension section
        if (view.getId() == suspension.getId()) {
            // Expand or collapse the section, and get the new visibility
            visibility = expandCollapseImageChangeMethod(suspensionExpanded, suspension);

            // Determine if items in section should be visible or gone now
            if (visibility == View.GONE)
                suspensionExpanded = false;
            else
                suspensionExpanded = true;

            // Set the new visibilities
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

        // Button for tires section
        if (view.getId() == tires.getId()) {
            // Expand or collapse the section, and get the new visibility
            visibility = expandCollapseImageChangeMethod(tiresExpanded, tires);

            // Determine if items in section should be visible or gone now
            if (visibility == View.GONE)
                tiresExpanded = false;
            else
                tiresExpanded = true;

            // Set the new visibilities
            frontTirePressureOutTitle.setVisibility(visibility);
            frontTirePressureInTitle.setVisibility(visibility);
            rearTirePressureOutTitle.setVisibility(visibility);
            rearTirePressureInTitle.setVisibility(visibility);
            frontTirePressureOutValue.setVisibility(visibility);
            frontTirePressureInValue.setVisibility(visibility);
            rearTirePressureOutValue.setVisibility(visibility);
            rearTirePressureInValue.setVisibility(visibility);

        }

        // Button for gearing section
        if (view.getId() == gearing.getId()) {
            // Expand or collapse the section, and get the new visibility
            visibility = expandCollapseImageChangeMethod(gearingExpanded, gearing);

            // Determine if items in section should be visible or gone now
            if (visibility == View.GONE)
                gearingExpanded = false;
            else
                gearingExpanded = true;

            // Set the new visibilities
            frontSprocketTitle.setVisibility(visibility);
            frontSprocketValue.setVisibility(visibility);
            rearSprocketTitle.setVisibility(visibility);
            rearSprocketValue.setVisibility(visibility);

        }

    }

    /**
     * Method used to hide the keyboard if the section gets closed while an edittext is selected
     * and the keyboard is still open
     * @param view
     */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Determine if section is expanding or collapsing, and change the imageview appropriately
     * @param expanded true if section already expanded, false if collapsed
     * @param view The imageview for the section
     * @return the new visibility for items in the section
     */
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

    /**
     * @param id the ID of the loader
     * @param args arguments sent to the loader
     * @return the cursor for the returned data
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, sentUri, DETAIL_COLUMNS, null, null, null);
    }

    /**
     * When the loader is finished, all data is loaded into it's appropriate place
     * @param loader the loader that called this method
     * @param data the cursor pointing to the data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() == 1) {

            // Move cursor to first (and should be only) point in the list
            data.moveToFirst();

            // Set all header information
            headerTrackName.setText(data.getString(COL_TRACK_NAME));
            headerTrackName.setContentDescription("Track name is " + data.getString(COL_TRACK_NAME));
            headerDate.setText(data.getString(COL_DATE));
            headerDate.setContentDescription("Date of session is " + data.getString(COL_DATE));
            headerSessionNumber.setText("Session " + data.getString(COL_SESSION_NUMBER));
            headerSessionNumber.setContentDescription("Session number is " + data.getString(COL_SESSION_NUMBER));
            sessionId = data.getInt(COL_SESSION_ID);

            // Get lap time information
            tempLapTimeList = data.getString(COL_LAP_TIMES);
            // If the list is not null, split it up into a string array
            if (tempLapTimeList != null) {
                lapTimeStringList = tempLapTimeList.split(";");
                // Send the string array to be put into textviews and added to the lap times layout
                addLapTimeTextViewsFromData(lapTimeStringList);
            }

            // Set all suspension information
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

            // Set all tire information
            frontTirePressureOutValue.setText(data.getString(COL_FRONT_TIRE_PRESSURE_OUT));
            frontTirePressureInValue.setText(data.getString(COL_FRONT_TIRE_PRESSURE_IN));
            rearTirePressureOutValue.setText(data.getString(COL_REAR_TIRE_PRESSURE_OUT));
            rearTirePressureInValue.setText(data.getString(COL_REAR_TIRE_PRESSURE_IN));

            // Set all gearing information
            frontSprocketValue.setText(data.getString(COL_FRONT_SPROCKET));
            rearSprocketValue.setText(data.getString(COL_REAR_SPROCKET));

        } else
            Log.v(LOG_TAG, "DATA COULD BE NULL, COUNT IS: " + data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Not used
    }


    /**
     * Set TextChangedListeners on all EditTexts
     * @param group
     */
    public void setListeners(EditText[] group) {
        for (int i = 0; i < group.length; i++) {
            if (group[i] == null)
                Log.v(LOG_TAG, "OBJECT EQUAL TO NULL!!!");
            else
                group[i].addTextChangedListener(new DetailsTextWatcher(group[i], this));
        }
    }

    /**
     * Figure out which data changed, and update it in the database
     * @param et
     */
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

        insertNewSessionData(cv);
    }

    /**
     * Opens a new dialog to add a new lap time
     * @param view
     */
    public void addLapTime(View view) {
        DialogFragment newDialog = new AddLapTimeDialog();
        newDialog.show(getSupportFragmentManager(), getString(R.string.add_track_dialog));
    }

    /**
     * Positive click method for add lap time dialog
     * @param dialog The dialog the button was clicked on
     */
    @Override
    public void OnDialogPositiveClick(DialogFragment dialog) {
        String lapTimeEntered;

        // Get reference to EditText where lap time was entered
        EditText lap_time_text = (EditText) dialog.getDialog().findViewById(R.id.lap_time_entry);
        // Get the text from the EditText
        lapTimeEntered = lap_time_text.getText().toString();
        // Add the lap time to the list
        lapTimeStringList = AddLapTimeToList(lapTimeEntered, lapTimeStringList);
    }

    /**
     * Negative click method for add lap time dialog
     * @param dialog The dialog the button was clicked on
     */
    @Override
    public void OnDialogNegativeClick(DialogFragment dialog) {
        // Not used
    }

    public String[] AddLapTimeToList(String lap_time_to_add, String[] currentStringList) {
        String[] newStringList;                     // New list to be returned
        ContentValues cv = new ContentValues();     // ContentValues to be inserted into DB
        String finalString;                         // The final lap time string for the DB

        // Layout params for the new lap time text view
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // New textview to be added
        TextView tv = new TextView(this);
        // Set the params of the new TextView
        tv.setLayoutParams(params);

        // Set new TextView text to lap time
        if (currentStringList != null)
            tv.setText("Lap " + (currentStringList.length + 1) + "- " + lap_time_to_add);
        else
            tv.setText("Lap 1 -  " + lap_time_to_add);

        // Add new TextView to the lap times linear layout
        lapTimesLinearLayout.addView(tv);

        // Add new lap time to the string array
        if (currentStringList == null) {
            // if null just make a new array list
            newStringList = new String[]{lap_time_to_add};
        } else {
            // if not null, copy over old list and add new lap time
            newStringList = new String[(currentStringList.length + 1)];
            for (int i = 0; i <= currentStringList.length; i++)
                if (i < currentStringList.length) {
                    newStringList[i] = currentStringList[i];
                } else {
                    newStringList[i] = lap_time_to_add;
                }
        }

        // Convert the array to a single String to be put in the DB
        finalString = ConvertStringListToString(newStringList);

        // Add the new lap times string to the contentvalues
        cv.put(DataContract.SessionsEntry.COLUMN_LAP_TIMES, finalString);

        // Send the contentvalues to this method to be inserted
        insertNewSessionData(cv);

        // Return the new string array of lap times
        return newStringList;
    }

    /**
     * This method takes the lap times data from the DB and creates TextViews for each lap time.
     * It then adds those textviews into the lap times linear layout.
     * @param lapTimeInfo
     */
    public void addLapTimeTextViewsFromData(String[] lapTimeInfo) {

        // The params for the new textviews
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Create a new textview for each lap time
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

    // Convert the string array to a single string of lap times, separated by semi-colons.
    public String ConvertStringListToString(String[] convertList) {
        String finalString = convertList[0] + ";";

        if (convertList != null)
            for (int i = 1; i < convertList.length; i++)
                finalString = finalString + (convertList[i] + ";");

        return finalString;
    }

    /**
     * Insert any new session data into DB
     * @param values
     */
    public void insertNewSessionData(ContentValues values) {
        String selection = DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry._ID + " = ?";
        String selectionArgs[] = new String[]{Integer.toString(sessionId)};
        Uri updateUri = DataContract.SessionsEntry.CONTENT_URI;

        getContentResolver().update(updateUri, values, selection, selectionArgs);

    }


}
