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
 * Created by silen_000 on 8/6/2016.
 */
public class AddTrackDialog extends DialogFragment {
    private static final String LOG_TAG = AddTrackDialog.class.getSimpleName();

    DataReturnInterface dataReturnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        EditText newTrack;

        builder.setTitle(R.string.add_track_title)

                .setView(inflater.inflate(R.layout.add_track_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.OK_button,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dataReturnActivity.onDialogPositiveClick(AddTrackDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataReturnActivity.onDialogNegativeClick(AddTrackDialog.this);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.v(LOG_TAG, "HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try{
            dataReturnActivity = (DataReturnInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the DataReturnInterface");
        }

    }

    public interface DataReturnInterface {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
