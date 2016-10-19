package com.michaelcavalli.trackrider.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This contact describes the tables that hold all track, track day, and session information
 */
public class DataContract {

    //Content authority
    public static final String CONTENT_AUTHORITY = "com.michaelcavalli.trackrider";

    // Base URI
    public static  final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TRACKS = "tracks";
    public static final String PATH_TRACK_DAYS = "track_days";
    public static final String PATH_SESSIONS = "sessions";


    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;

        // Table name
        public static final String TABLE_NAME = "tracks";

        // Track name
        public static final String COLUMN_TRACK_NAME = "track_name";

        // Longitude
        public static final String COLUMN_LONGITUDE = "track_longitude";

        // Latitude
        public static final String COLUMN_LATITUDE = "track_latitude";


        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrackListUri(){
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the trackdays table */
    public static final class TrackDays implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK_DAYS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK_DAYS;

        // Table name
        public static final String TABLE_NAME = "track_days";

        // Trackday name
        public static final String COLUMN_TRACK_DAY_NAME = "trackday_name";

        // Track name
        public static final String COLUMN_TRACK_NAME = "track_name";

        // Track key
        public static final String COLUMN_TRACK_KEY = "track_key";

        // Track Day Date
        public static final String COLUMN_TRACK_DAY_DATE = "track_day_date";

        public static Uri buildTrackDayUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrackDays(){
            return CONTENT_URI;
        }
    }


    /* Inner class that defines the table contents of the sessions table */
    public static final class SessionsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SESSIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SESSIONS;

        // Table name
        public static final String TABLE_NAME = "sessions";

        // lap  times
        public static final String COLUMN_LAP_TIMES = "lap_times";

        // Track name
        public static final String COLUMN_TRACK_NAME = "Strack_name";

        // Track key
        public static final String COLUMN_TRACK_DAY_KEY = "track_id";
        // Date
        public static final String COLUMN_DATE = "date";
        // Sessions #
        public static final String COLUMN_SESSIONS_NUMBER = "session_number";

        /* Suspension Information */
        // Front Compression
        public static final String COLUMN_FRONT_COMPRESSION = "front_compression";
        // Front Rebound
        public static final String COLUMN_FRONT_REBOUND = "front_rebound";
        // Front Preload
        public static final String COLUMN_FRONT_PRELOAD = "front_preload";
        // Fork Height
        public static final String COLUMN_FORK_HEIGHT = "fork_height";
        // Oil Height
        public static final String COLUMN_OIL_HEIGHT = "oil_height";
        // Front Spring Rate
        public static final String COLUMN_FRONT_SPRING_RATE = "front_spring_rate";
        // Rear Compression
        public static final String COLUMN_REAR_COMPRESSION = "rear_compression";
        // Rear Rebound
        public static final String COLUMN_REAR_REBOUND = "rear_rebound";
        // Rear Preload
        public static final String COLUMN_REAR_PRELOAD = "rear_preload";
        // Rear Height
        public static final String COLUMN_REAR_HEIGHT = "rear_height";
        // Rear Spring Rate
        public static final String COLUMN_REAR_SPRING_RATE = "rear_spring_rate";

        /* Tire Information */
        // Front Tire Pressure Out
        public static final String COLUMN_FRONT_TIRE_PRESSURE_OUT = "front_tire_pressure_out";
        // Front Tire Pressure In
        public static final String COLUMN_FRONT_TIRE_PRESSURE_IN = "front_tire_pressure_in";
        // Rear Tire Pressure Out
        public static final String COLUMN_REAR_TIRE_PRESSURE_OUT = "rear_tire_pressure_out";
        // Rear Tire Pressure In
        public static final String COLUMN_REAR_TIRE_PRESSURE_IN = "rear_tire_pressure_in";

        /* Gearing Information */
        // Front Sprocket
        public static final String COLUMN_FRONT_SPROCKET = "front_sprocket";
        // Rear Sprocket
        public static final String COLUMN_REAR_SPROCKET = "rear_sprocket";

        // For returning location of entry into table
        public static Uri buildSessionUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //
        public static Uri buildSessionsWithTrackDayId(int trackDayId){
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(trackDayId)).build();
        }

        public static Uri buildSessionWithTrackDayIdAndSessionNumber(String trackDayId, String sessionNumber){
            return CONTENT_URI.buildUpon().appendPath(trackDayId).appendPath(sessionNumber).build();
        }

        public static String getTrackDayIDFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }


        public static String getSessionNumberFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
    }

}
