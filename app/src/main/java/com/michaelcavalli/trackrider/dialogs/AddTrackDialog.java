package com.michaelcavalli.trackrider.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProvider;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.michaelcavalli.trackrider.R;

/**
 * The dialog for adding tracks to the track list
 */
public class AddTrackDialog extends DialogFragment {
    private static final String LOG_TAG = AddTrackDialog.class.getSimpleName();

    // The return interface, for returning track information
    DataReturnInterface dataReturnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        EditText newTrack;

        builder.setTitle(R.string.add_track_title)
                // Create the dialog using the add_track_dialog layout
                .setView(inflater.inflate(R.layout.add_track_dialog, null))
                .setPositiveButton(R.string.OK_button,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Call the return interface method with a reference to this dialog
                        dataReturnActivity.onDialogPositiveClick(AddTrackDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Call the return interface method with a reference to this dialog
                        dataReturnActivity.onDialogNegativeClick(AddTrackDialog.this);
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
            dataReturnActivity = (DataReturnInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the DataReturnInterface");
        }

    }

    /**
     * Interface that must be implemented by activity that creates this dialog
     */
    public interface DataReturnInterface {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
