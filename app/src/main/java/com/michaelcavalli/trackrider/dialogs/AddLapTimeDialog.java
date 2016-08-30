package com.michaelcavalli.trackrider.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.michaelcavalli.trackrider.R;

/**
 * This dialog is used to add a lap time to a session
 */
public class AddLapTimeDialog extends DialogFragment {
    private static final String LOG_TAG = AddLapTimeDialog.class.getSimpleName();

    // Interface to return the lap time to
    ReturnLapTimeInterface returnLapTimeActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        EditText new_lap_time;

        // Build the dialog
        builder.setView(inflater.inflate(R.layout.add_lap_time_dialog, null))
                .setPositiveButton(R.string.OK_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // call the return interface method with a reference to this dialog
                        returnLapTimeActivity.OnDialogPositiveClick(AddLapTimeDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing if cancelled
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
            // Activity dialog is attached to has to be a ReturnLapTimeInterface
            returnLapTimeActivity = (ReturnLapTimeInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " MUST IMPLEMENT THE RETURN LAP TIME INTERFACE");
        }
    }

    /**
     * Interface that must be implemented by activity that creates this dialog
     */
    public interface ReturnLapTimeInterface{
        public void OnDialogPositiveClick(DialogFragment dialog);
        public void OnDialogNegativeClick(DialogFragment dialog);
    }
}
