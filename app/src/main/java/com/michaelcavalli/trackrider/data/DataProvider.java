package com.michaelcavalli.trackrider.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by silen_000 on 8/6/2016.
 */
public class DataProvider extends ContentProvider {
    private static final String LOG_TAG = DataProvider.class.getSimpleName();


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DataDbHelper mDataHelper;

    static final int TRACKS = 100;
    static final int TRACKDAYS = 101;
    static final int SESSIONS = 102;
    static final int SESSIONS_WITH_TRACKDAY_ID= 200;
    static final int SESSION_WITH_TRACKDAY_ID_AND_NUMBER = 201;

    // Query data between the track list and track day table
    private static final SQLiteQueryBuilder dataQueryBuilder;

    // Query data between the track day and sessions table
    private static final SQLiteQueryBuilder sessionsQueryBuilder;

    static {
        dataQueryBuilder = new SQLiteQueryBuilder();
        sessionsQueryBuilder = new SQLiteQueryBuilder();

        // Table for data between the track list and track days tables
        dataQueryBuilder.setTables(
                DataContract.TrackDays.TABLE_NAME + " INNER JOIN " +
                        DataContract.TrackEntry.TABLE_NAME +
                        " ON " + DataContract.TrackDays.TABLE_NAME +
                        "." + DataContract.TrackDays.COLUMN_TRACK_KEY +
                        " = " + DataContract.TrackEntry.TABLE_NAME +
                        "." + DataContract.TrackEntry._ID);

        // Table for data between the track day and sessions tables
        sessionsQueryBuilder.setTables(
                DataContract.SessionsEntry.TABLE_NAME + " INNER JOIN " +
                        DataContract.TrackDays.TABLE_NAME +
                        " ON " + DataContract.SessionsEntry.TABLE_NAME +
                        "." + DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY +
                        " = " + DataContract.TrackDays.TABLE_NAME +
                        "." + DataContract.TrackDays._ID
        );
    }

    // For returning list of sessions for a specific track day
    private static final String trackDaySelection =
            DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY + " = ? ";

    // For returning data from a specific session based on track day key and session number
    private static final String sessionSelection =
            DataContract.SessionsEntry.TABLE_NAME + "." + DataContract.SessionsEntry.COLUMN_TRACK_DAY_KEY + " = ? AND " +
            DataContract.SessionsEntry.COLUMN_SESSIONS_NUMBER + " = ? ";

    /**
     * @param sortOrder order to return tracks in
     * @param projection columns to return
     * @return cursor with a list of tracks for tracks list activity
     */
    private Cursor getTrackList(String sortOrder, String[] projection) {
        return mDataHelper.getReadableDatabase().query(
                DataContract.TrackEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
                );
    }

    /**
     * @param sortOrder order is by date
     * @param projection columns to return
     * @return cursor with a list of all track days to main activity
     */
    private Cursor getTrackDays(String sortOrder, String[] projection) {
        return mDataHelper.getReadableDatabase().query(
                DataContract.TrackDays.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    /**
     * @param uri database to search
     * @param projection columns to return
     * @param sortOrder order is by session number
     * @return cursor with a list of sessions for a specific track day
     */
    private Cursor getSessionsList(Uri uri, String[] projection, String sortOrder) {
        String trackDayId = DataContract.SessionsEntry.getTrackDayIDFromUri(uri);

        String[] selectionArgs = new String[]{trackDayId};
        String selection = trackDaySelection;

        return sessionsQueryBuilder.query(mDataHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    /**
     * @param uri database to search
     * @param projection cloumns to return
     * @return cursor pointing to a specific session on a specific track day
     */
    private Cursor getSessionData(Uri uri, String[] projection) {
        String trackDayId = DataContract.SessionsEntry.getTrackDayIDFromUri(uri);
        String sessionNumber = DataContract.SessionsEntry.getSessionNumberFromUri(uri);

        String[] selectionArgs = new String[]{trackDayId, sessionNumber};
        String selection = sessionSelection;

        return sessionsQueryBuilder.query(mDataHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    /**
     *  builds uri matcher to figure out what request is searching for
     * @return
     */
    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // To query list of tracks and add to it
        matcher.addURI(authority, DataContract.PATH_TRACKS, TRACKS);
        // To query list of track days and add to it
        matcher.addURI(authority, DataContract.PATH_TRACK_DAYS, TRACKDAYS);
        // To add to sessions table
        matcher.addURI(authority, DataContract.PATH_SESSIONS, SESSIONS);
        // To query list of sessions for a specific track day, can return 0 to multiple rows
        matcher.addURI(authority, DataContract.PATH_SESSIONS + "/*", SESSIONS_WITH_TRACKDAY_ID);
        // To query list of sessions for data from a specific session, returns only one session.
        matcher.addURI(authority, DataContract.PATH_SESSIONS + "/*/*", SESSION_WITH_TRACKDAY_ID_AND_NUMBER);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDataHelper = new DataDbHelper(getContext());
        return true;
    }

    /**
     * @param uri database to search
     * @param projection columns to return
     * @param selection specific selection in database
     * @param selectionArgs argument for selections
     * @param sortOrder sort order requested
     * @return cursor of selection
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case TRACKS:
                retCursor = getTrackList(sortOrder, projection);
                break;
            case TRACKDAYS:
                retCursor = getTrackDays(sortOrder, projection);
                break;
            case SESSIONS_WITH_TRACKDAY_ID:
                retCursor = getSessionsList(uri, projection, sortOrder);
                break;
            case SESSION_WITH_TRACKDAY_ID_AND_NUMBER:
                retCursor = getSessionData(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * @param uri database search request
     * @return either content_type or content_item_type
     */
    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TRACKS:
                return DataContract.TrackEntry.CONTENT_TYPE;
            case TRACKDAYS:
                return DataContract.TrackDays.CONTENT_TYPE;
            case SESSIONS:
                return DataContract.SessionsEntry.CONTENT_TYPE;
            case SESSIONS_WITH_TRACKDAY_ID:
                return DataContract.SessionsEntry.CONTENT_TYPE;
            case SESSION_WITH_TRACKDAY_ID_AND_NUMBER:
                return DataContract.SessionsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Uknown uri: " + uri);
        }
    }

    /**
     * @param uri database to insert into
     * @param values values to insert into database
     * @return Uri pointing to new data in database
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mDataHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case TRACKS: {
                long _id = db.insert(DataContract.TrackEntry.TABLE_NAME, null, values);
                if(_id > 0)
                    returnUri = DataContract.TrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRACKDAYS: {
                long _id = db.insert(DataContract.TrackDays.TABLE_NAME, null, values);
                if(_id > 0 )
                    returnUri = DataContract.TrackDays.buildTrackDayUri(_id);
                else
                    throw new android.database.SQLException("Unable to insert row into " + uri);
                break;
            }
            case SESSIONS: {
                long _id = db.insert(DataContract.SessionsEntry.TABLE_NAME, null, values);
                if(_id > 0)
                    returnUri = DataContract.SessionsEntry.buildSessionUri(_id);
                else
                    throw new android.database.SQLException("Unable to insert new row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     * @param uri database
     * @param selection specific selection to be deleted
     * @param selectionArgs arguments for selection
     * @return the number of rows deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDataHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if(null == selection) selection = "1";

        switch (match) {
            case TRACKS:
                rowsDeleted = db.delete(DataContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACKDAYS:
                rowsDeleted = db.delete(DataContract.TrackDays.TABLE_NAME, selection, selectionArgs);
                break;
            case SESSIONS:
                rowsDeleted = db.delete(DataContract.SessionsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * @param uri database to update
     * @param values values to update
     * @param selection specific selection to search for
     * @param selectionArgs arguments for selection
     * @return the number of rows updated
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDataHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case TRACKS:
                rowsUpdated = db.update(DataContract.TrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRACKDAYS:
                rowsUpdated = db.update(DataContract.TrackDays.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SESSIONS:
                rowsUpdated = db.update(DataContract.SessionsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0 && match != SESSIONS){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
