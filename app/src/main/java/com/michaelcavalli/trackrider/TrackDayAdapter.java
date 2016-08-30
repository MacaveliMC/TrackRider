package com.michaelcavalli.trackrider;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This is the adapter for the recyclerview on the Main Activity to list track days from the DB
 */
public class TrackDayAdapter extends RecyclerView.Adapter<TrackDayAdapter.TrackDayViewHolder> {
    private static final String LOG_TAG = TrackDayAdapter.class.getSimpleName();

    private Cursor trackDaysCursor;                         // The cursor for the track day data
    private TrackDayOnClickHandler trackDayClickHandler;    // The reference to send clicks to the activity

    /**
     * This is the viewholder for the track day adapter
     */
    public class TrackDayViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        public final TextView trackName;    // The header textview for the track name
        public final TextView date;         // The header  textview for the date
        public int id;                      // The id of the track day

        public TrackDayViewHolder(View view){
            super(view);
            // Get the textview references in the list item
            trackName = (TextView) view.findViewById(R.id.track_name);
            date = (TextView) view.findViewById(R.id.trackday_date);
            // Set the onclick listeners for the list item
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        /**
         * When a view is long clicked, send it back to the activity for delete option
         * @param view The view long clicked
         * @return
         */
        @Override
        public boolean onLongClick(View view) {
            return trackDayClickHandler.onClick(this, true);
        }

        /**
         * If the view is clicked, sent it back to the activity to open the sessions activity
         * for this track day
         * @param view The view clicked
         */
        @Override
        public void onClick(View view) {
            trackDayClickHandler.onClick(this, false);
        }
    }

    /**
     * The interface that must be implemented by the activity using this adapter
     */
    public static interface TrackDayOnClickHandler {
        boolean onClick(TrackDayViewHolder vh, boolean longClick);
    }

    /**
     * Constructor for the adapter, with reference to the activity as the click handler
     * @param ch
     */
    public TrackDayAdapter(TrackDayOnClickHandler ch){
        trackDayClickHandler=ch;
    }

    /**
     * This method creates and returns a new view holder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public TrackDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            // The view to use as a list item, inflated in the parent
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trackday_list_item, parent, false);
            return new TrackDayViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /**
     * This method gets the data from the cursor for the requested position, and uses the references
     * in the view holder to fill in the layout
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(TrackDayViewHolder holder, int position) {
        // Move the cursor to the position that is being requested
        trackDaysCursor.moveToPosition(position);

        // Get all the info needed
        int id = trackDaysCursor.getInt(MainActivity.COL_TRACK_ENTRY_ID);
        String trackName = trackDaysCursor.getString(MainActivity.COL_TRACK_NAME);
        String date = trackDaysCursor.getString(MainActivity.COL_TRACK_DAY_DATE);

        // Set the values into the layout using the holder
        holder.id=id;
        holder.trackName.setText(trackName);
        holder.trackName.setContentDescription("Track for this track day is " + trackName);
        holder.date.setText(date);
        holder.date.setContentDescription("Date for this track day is " + date);
    }

    /**
     * Returns the number of items in the list
     * @return
     */
    @Override
    public int getItemCount() {
        if(trackDaysCursor == null)
            return 0;
        else
            return trackDaysCursor.getCount();
    }

    /**
     * Swaps the cursor for a new one
     * @param newCursor
     */
    public void swapCursor(Cursor newCursor){
        trackDaysCursor = newCursor;
        // Notify that the data set has changed.
        notifyDataSetChanged();
    }
}
