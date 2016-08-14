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
 * Created by silen_000 on 8/6/2016.
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackListAdapterViewHolder> {

    private static final String LOG_TAG = TrackListAdapter.class.getSimpleName();
    private Cursor trackListCursor;
    final private TrackListAdapterOnClickHandler listClickHandler;
    private final Context context;


    public class TrackListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public final TextView trackName;
        public final TextView gpsLocation;
        public String fullGpsLocation;

        public TrackListAdapterViewHolder(View view) {
            super(view);
            trackName = (TextView) view.findViewById(R.id.track_name);
            gpsLocation = (TextView) view.findViewById(R.id.gps_location);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            return listClickHandler.onClick(this, true);
        }

        @Override
        public void onClick(View view) {
            listClickHandler.onClick(this, false);
        }
    }

    public static interface TrackListAdapterOnClickHandler {
        boolean onClick(TrackListAdapterViewHolder vh, boolean longClick);
    }

    public TrackListAdapter(Context c, TrackListAdapterOnClickHandler dh) {
        listClickHandler = dh;
        context = c;
    }

    @Override
    public TrackListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_layout, parent, false);
            return new TrackListAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(TrackListAdapterViewHolder holder, int position) {
        trackListCursor.moveToPosition(position);
        String trackName = trackListCursor.getString(TrackListActivity.COL_TRACK_NAME);
        String longitude = trackListCursor.getString(TrackListActivity.COL_TRACK_LONGITUDE);
        String latitude = trackListCursor.getString(TrackListActivity.COL_TRACK_LATITUDE);
        holder.trackName.setText(trackName);
        holder.trackName.setContentDescription(trackName);
        if (longitude != null & latitude != null) {
            holder.gpsLocation.setText(latitude.substring(0, 8) + ", " + longitude.substring(0, 8));
            holder.gpsLocation.setContentDescription(context.getString(R.string.gps_textview_desc));
            holder.fullGpsLocation = latitude + ", " + longitude;
        }


    }

    @Override
    public int getItemCount() {
        if (trackListCursor == null)
            return 0;
        else
            return trackListCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        trackListCursor = newCursor;
        notifyDataSetChanged();
    }
}
