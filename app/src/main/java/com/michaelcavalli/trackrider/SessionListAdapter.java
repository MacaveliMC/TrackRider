package com.michaelcavalli.trackrider;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by silen_000 on 8/9/2016.
 */
public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.SessionViewHolder> {
    private static final String LOG_TAG = SessionListAdapter.class.getSimpleName();
    private Cursor sessionListCursor;
    private SessionListOnClickHandler sessionListClickHandler;
    private Context context;

    public class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView sessionNumberView;
        public int sessionNumber;
        public int sessionId;
        public int trackDayKey;

        public SessionViewHolder (View view){
            super(view);
            sessionNumberView = (TextView) view.findViewById(R.id.session_number);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            sessionListClickHandler.onClick(this, false);
        }

        @Override
        public boolean onLongClick(View view) {
            return sessionListClickHandler.onClick(this, true);
        }
    }

    public static interface SessionListOnClickHandler{
        boolean onClick(SessionViewHolder vh, boolean longClick);
    }

    public SessionListAdapter (Context c, SessionListOnClickHandler ch){
        context=c;
        sessionListClickHandler = ch;
    }

    @Override
    public SessionListAdapter.SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_item, parent, false);
            return new SessionViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(SessionListAdapter.SessionViewHolder holder, int position) {
        sessionListCursor.moveToPosition(position);

        holder.sessionNumber = sessionListCursor.getInt(SessionsActivity.COL_SESSION_NUMER);
        int sessionId = sessionListCursor.getInt(SessionsActivity.COL_SESSION_ID);
        int track_day_id = sessionListCursor.getInt(SessionsActivity.COL_TRACK_DAY_KEY);

        holder.sessionNumberView.setText("Session " + holder.sessionNumber);
        holder.sessionNumberView.setContentDescription("Session number " + holder.sessionNumber);
        holder.sessionId = sessionId;
        holder.trackDayKey = track_day_id;

    }

    @Override
    public int getItemCount() {
        if(sessionListCursor == null){
            return 0;
        } else
            return sessionListCursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        sessionListCursor = newCursor;
        if(sessionListCursor != null){
            notifyDataSetChanged();
        }
    }
}
