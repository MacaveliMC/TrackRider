package com.michaelcavalli.trackrider.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.michaelcavalli.trackrider.MainActivity;
import com.michaelcavalli.trackrider.R;
import com.michaelcavalli.trackrider.data.DataContract;

/**
 * The widget that displays track days
 */
public class TrackRiderWidgetProvider extends AppWidgetProvider {

    /**
     * On system update reqeusts, updates any widgets that exist
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //How many widgets need to be updated
        final int N = appWidgetIds.length;

        // Context used to get data provider
        Context c = context;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            String trackName;
            String trackDate;
            String sortOrder = DataContract.TrackDays.TABLE_NAME + "." + DataContract.TrackDays._ID + " ASC";

            Cursor trackDays = c.getContentResolver().query(DataContract.TrackDays.CONTENT_URI, null, null, null, sortOrder);

            // Make sure cursor is not null
            if (trackDays != null) {
                // If cursor is not null, make sure data exists
                if (trackDays.getCount() > 0) {
                    // Move to first to get info from latest track day
                    trackDays.moveToFirst();
                    // Fill info from latest track day
                    trackName = trackDays.getString(1);
                    trackDate = trackDays.getString(3);
                }
                // If no data, fill with default info
                else {
                    trackName = "NO TRACK DAYS";
                    trackDate = "";
                }
            }
            // If cursor is null, fill with default data
            else {
                trackName = "NO TRACK DAYS";
                trackDate = "";
            }

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.trackrider_appwidget);
            RemoteViews lv = new RemoteViews(context.getPackageName(), R.layout.trackday_list_item);

            // Add data to textviews
            lv.setTextViewText(R.id.track_name, trackName);
            lv.setTextViewText(R.id.trackday_date, trackDate);

            // Add view for track day to layout
            views.addView(R.id.widget_linear_layout, lv);

            // Set the onclick listener to the view
            views.setOnClickPendingIntent(R.id.widget_linear_layout, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
