package com.michaelcavalli.trackrider.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.michaelcavalli.trackrider.R;

/**
 * Created by silen_000 on 10/18/2016.
 */

public class TrackNameChangeDialog extends DialogFragment {
    private static final String LOG_TAG = TrackNameChangeDialog.class.getSimpleName();

    // The return interface, for returning track information
    NameChangeReturnInterface dataReturnActivity;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.user_enter_info)
                // Create the dialog using the add_track_dialog layout
                .setView(inflater.inflate(R.layout.change_trackday_name_dialog, null))
                .setPositiveButton(R.string.OK_button,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Call the return interface method with a reference to this dialog
                        dataReturnActivity.changeTrackDayName(TrackNameChangeDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing
                    }
                });

        return builder.create();
    }

    /**
     * On attaching the dialog to the activity, pass the activity as the returning interface
     * @param context the activity this dialog is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            // Activity dialog is attached to has to be a DataReturnInterface
            dataReturnActivity = (TrackNameChangeDialog.NameChangeReturnInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the NameChangeReturnInterface");
        }
    }


    /**
     * Interface that must be implemented by activity that creates this dialog
     */
    public interface NameChangeReturnInterface {
        public void changeTrackDayName(DialogFragment dialog);
    }

}
