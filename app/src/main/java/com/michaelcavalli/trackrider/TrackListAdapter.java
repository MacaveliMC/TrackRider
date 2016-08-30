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
 * This is the adapter for the track list activity
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackListAdapterViewHolder> {
    private static final String LOG_TAG = TrackListAdapter.class.getSimpleName();

    private Cursor trackListCursor;                                 // Cursor for track list info
    final private TrackListAdapterOnClickHandler listClickHandler;  // Click handler for track clicks
    private final Context context;                                  // Context of activity


    /**
     * This is the view holder for this adapter
     */
    public class TrackListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public final TextView trackName;    // TextView in the list item layout for the track name
        public final TextView gpsLocation;  // TextView holding GPS info
        public String fullGpsLocation;      // Full string of GPS info

        public TrackListAdapterViewHolder(View view) {
            super(view);
            // Get TextView references in list item layout
            trackName = (TextView) view.findViewById(R.id.track_name);
            gpsLocation = (TextView) view.findViewById(R.id.gps_location);
            // Set click listeners
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        /**
         * Called if a list item is long clicked.  Method in the click handler is called.
         * @param v
         * @return
         */
        @Override
        public boolean onLongClick(View v) {
            return listClickHandler.onClick(this, true);
        }

        /**
         * Called if a list item is clicked. Method in the click handler is called.
         * @param view
         */
        @Override
        public void onClick(View view) {
            listClickHandler.onClick(this, false);
        }
    }

    /**
     * Interface used to send call backs
     */
    public static interface TrackListAdapterOnClickHandler {
        boolean onClick(TrackListAdapterViewHolder vh, boolean longClick);
    }

    /**
     * Constructor for the adapter.  Recieves the click handler reference and context of the activity
     * @param c
     * @param dh
     */
    public TrackListAdapter(Context c, TrackListAdapterOnClickHandler dh) {
        listClickHandler = dh;
        context = c;
    }

    /**
     * Creates and returns the viewholder for a requested list item
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public TrackListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            // The view to use as a list item, inflated in the parent
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_layout, parent, false);
            return new TrackListAdapterViewHolder(view);
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
    public void onBindViewHolder(TrackListAdapterViewHolder holder, int position) {
        // Move the cursor to the position that is being requested
        trackListCursor.moveToPosition(position);
        // Get all the info needed
        String trackName = trackListCursor.getString(TrackListActivity.COL_TRACK_NAME);
        String longitude = trackListCursor.getString(TrackListActivity.COL_TRACK_LONGITUDE);
        String latitude = trackListCursor.getString(TrackListActivity.COL_TRACK_LATITUDE);

        // Set the values into the layout using the holder
        holder.trackName.setText(trackName);
        holder.trackName.setContentDescription(trackName);

        // Check if GPS info exists, and if so insert it into view
        if (longitude != null & latitude != null) {
            holder.gpsLocation.setText(latitude.substring(0, 8) + ", " + longitude.substring(0, 8));
            holder.gpsLocation.setContentDescription(context.getString(R.string.gps_textview_desc));
            holder.fullGpsLocation = latitude + ", " + longitude;
        }


    }

    /**
     * Returns the number of items in the list
     * @return
     */
    @Override
    public int getItemCount() {
        if (trackListCursor == null)
            return 0;
        else
            return trackListCursor.getCount();
    }

    /**
     * Swaps the adapter cursor for a new one
     * @param newCursor
     */
    public void swapCursor(Cursor newCursor) {
        trackListCursor = newCursor;
        // Notify the data set has changed
        notifyDataSetChanged();
    }
}
