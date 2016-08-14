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
 * Created by silen_000 on 8/7/2016.
 */
public class TrackDayAdapter extends RecyclerView.Adapter<TrackDayAdapter.TrackDayViewHolder> {
    private static final String LOG_TAG = TrackDayAdapter.class.getSimpleName();
    private Cursor trackDaysCursor;
    private TrackDayOnClickHandler trackDayClickHandler;
    private Context context;


    public class TrackDayViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        public final TextView trackName;
        public final TextView date;
        public int id;

        public TrackDayViewHolder(View view){
            super(view);
            trackName = (TextView) view.findViewById(R.id.track_name);
            date = (TextView) view.findViewById(R.id.trackday_date);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            return trackDayClickHandler.onClick(this, true);
        }

        @Override
        public void onClick(View view) {
            trackDayClickHandler.onClick(this, false);
        }
    }

    public static interface TrackDayOnClickHandler {
        boolean onClick(TrackDayViewHolder vh, boolean longClick);
    }

    public TrackDayAdapter(Context c, TrackDayOnClickHandler ch){
        context=c;
        trackDayClickHandler=ch;
    }

    @Override
    public TrackDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trackday_list_item, parent, false);
            return new TrackDayViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(TrackDayViewHolder holder, int position) {
        trackDaysCursor.moveToPosition(position);

        int id = trackDaysCursor.getInt(MainActivity.COL_TRACK_ENTRY_ID);
        String trackName = trackDaysCursor.getString(MainActivity.COL_TRACK_NAME);
        String date = trackDaysCursor.getString(MainActivity.COL_TRACK_DAY_DATE);

        holder.id=id;
        holder.trackName.setText(trackName);
        holder.trackName.setContentDescription("Track for this track day is " + trackName);
        holder.date.setText(date);
        holder.date.setContentDescription("Date for this track day is " + date);
    }

    @Override
    public int getItemCount() {
        if(trackDaysCursor == null)
            return 0;
        else
            return trackDaysCursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        trackDaysCursor = newCursor;
        notifyDataSetChanged();
    }
}
