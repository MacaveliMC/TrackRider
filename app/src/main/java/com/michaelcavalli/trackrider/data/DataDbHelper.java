package com.michaelcavalli.trackrider.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.michaelcavalli.trackrider.data.DataContract.SessionsEntry;
import com.michaelcavalli.trackrider.data.DataContract.TrackDays;
import com.michaelcavalli.trackrider.data.DataContract.TrackEntry;

/**
 * Created by silen_000 on 8/6/2016.
 */
public class DataDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "track_data.db";

    public DataDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /* table for list of tracks */
        final String SQL_CREATE_TRACKLIST_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY," +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT UNIQUE ON CONFLICT REPLACE NOT NULL, " +
                TrackEntry.COLUMN_LONGITUDE + " TEXT, " +
                TrackEntry.COLUMN_LATITUDE + " TEXT " +
                ");";

        /* table for list of track days */
        final String SQL_CREATE_TRACKDAYS_TABLE = "CREATE TABLE " + TrackDays.TABLE_NAME + " (" +
                TrackDays._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrackDays.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                TrackDays.COLUMN_TRACK_KEY + " INTEGER NOT NULL, " +
                TrackDays.COLUMN_TRACK_DAY_DATE + " INT NOT NULL, " +

                // track key must always reference a specific track in the track entry table
                " FOREIGN KEY (" + TrackDays.COLUMN_TRACK_KEY + ") REFERENCES " +
                TrackEntry.TABLE_NAME + " (" + TrackEntry._ID + ") " +
                " );";

        /* table for list of sessions and session data */
        final String SQL_CREATE_SESSIONS_TABLE = "CREATE TABLE " + SessionsEntry.TABLE_NAME + " (" +

                SessionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SessionsEntry.COLUMN_TRACK_DAY_KEY + " INTEGER NOT NULL, " +
                SessionsEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                SessionsEntry.COLUMN_DATE + " INT NOT NULL, " +
                SessionsEntry.COLUMN_SESSIONS_NUMBER + " INTEGER NOT NULL, " +

                /* Lap Times */
                SessionsEntry.COLUMN_LAP_TIMES + " TEXT," +

                /* Suspension Information */
                SessionsEntry.COLUMN_FRONT_COMPRESSION + " TEXT, " +
                SessionsEntry.COLUMN_FRONT_REBOUND + " TEXT, " +
                SessionsEntry.COLUMN_FRONT_PRELOAD + " TEXT, " +
                SessionsEntry.COLUMN_FORK_HEIGHT + " TEXT, " +
                SessionsEntry.COLUMN_OIL_HEIGHT + " TEXT, " +
                SessionsEntry.COLUMN_FRONT_SPRING_RATE + " TEXT, " +
                SessionsEntry.COLUMN_REAR_COMPRESSION + " TEXT, " +
                SessionsEntry.COLUMN_REAR_REBOUND + " TEXT, " +
                SessionsEntry.COLUMN_REAR_PRELOAD + " TEXT, " +
                SessionsEntry.COLUMN_REAR_HEIGHT + " TEXT, " +
                SessionsEntry.COLUMN_REAR_SPRING_RATE + " TEXT, " +

                /* Tire Information */
                SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_OUT + " TEXT, " +
                SessionsEntry.COLUMN_FRONT_TIRE_PRESSURE_IN + " TEXT, " +
                SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_OUT + " TEXT, " +
                SessionsEntry.COLUMN_REAR_TIRE_PRESSURE_IN + " TEXT, " +

                /* Gearing Information */
                SessionsEntry.COLUMN_FRONT_SPROCKET + " TEXT, " +
                SessionsEntry.COLUMN_REAR_SPROCKET + " TEXT, " +

                // Track day key must always reference a specific track day in the track day table
                "FOREIGN KEY (" + SessionsEntry.COLUMN_TRACK_DAY_KEY + ") REFERENCES " +
                TrackDays.TABLE_NAME + " (" + TrackDays._ID + "), " +

                // Session number and track day key should be unique combination
                " UNIQUE (" + SessionsEntry.COLUMN_TRACK_DAY_KEY + ", " +
                SessionsEntry.COLUMN_SESSIONS_NUMBER + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_TRACKLIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRACKDAYS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SESSIONS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
