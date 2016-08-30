package com.michaelcavalli.trackrider;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This is the adapter for the session list activity
 */
public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.SessionViewHolder> {
    private static final String LOG_TAG = SessionListAdapter.class.getSimpleName();

    private Cursor sessionListCursor;                           // Cursor for list of sessions
    private SessionListOnClickHandler sessionListClickHandler;  // The call back for clicking on a session
    private Context context;                                    // The context of the session activity

    // The viewholder for this recyclerview
    public class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView sessionNumberView;      // This textview displays the session number
        public int sessionNumber;               // This is the session number
        public int sessionId;                   // This is the session ID
        public int trackDayKey;                 // This is the track day ID

        public SessionViewHolder (View view){
            super(view);
            sessionNumberView = (TextView) view.findViewById(R.id.session_number);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        /**
         * This method sends clicks back to the click handler activity through the onClick method.
         * @param view The view that was clicked
         */
        @Override
        public void onClick(View view) {
            sessionListClickHandler.onClick(this, false);
        }

        /**
         * This method sends long clicks back to the click handler activity through the onClick method.
         * @param view The view that was long clicked
         * @return
         */
        @Override
        public boolean onLongClick(View view) {
            return sessionListClickHandler.onClick(this, true);
        }
    }

    // The interface that must be implemented
    public static interface SessionListOnClickHandler{
        boolean onClick(SessionViewHolder vh, boolean longClick);
    }

    public SessionListAdapter (Context c, SessionListOnClickHandler ch){
        context=c;
        sessionListClickHandler = ch;
    }

    /**
     * This method creates the viewholder and returns it
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_item, parent, false);
            return new SessionViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /**
     * This method binds the appropriate data from the cursor to the viewholder based on position
     * @param holder The viewholder to bind data to
     * @param position The position of the viewholder
     */
    @Override
    public void onBindViewHolder(SessionListAdapter.SessionViewHolder holder, int position) {
        // Move to the position being binded
        sessionListCursor.moveToPosition(position);

        //  Set the viewholder info and get info from cursor
        holder.sessionNumber = sessionListCursor.getInt(SessionsActivity.COL_SESSION_NUMER);
        int sessionId = sessionListCursor.getInt(SessionsActivity.COL_SESSION_ID);
        int track_day_id = sessionListCursor.getInt(SessionsActivity.COL_TRACK_DAY_KEY);

        // Set the info in the layout and viewholder
        holder.sessionNumberView.setText("Session " + holder.sessionNumber);
        holder.sessionNumberView.setContentDescription("Session number " + holder.sessionNumber);
        holder.sessionId = sessionId;
        holder.trackDayKey = track_day_id;

    }

    /**
     * Returns item count of adapter cursor
     * @return
     */
    @Override
    public int getItemCount() {
        if(sessionListCursor == null){
            return 0;
        } else
            return sessionListCursor.getCount();
    }

    /**
     * Swaps the cursor for a new one and notifies the data set changed.
     * @param newCursor
     */
    public void swapCursor(Cursor newCursor){
        sessionListCursor = newCursor;
        if(sessionListCursor != null){
            notifyDataSetChanged();
        }
    }
}
