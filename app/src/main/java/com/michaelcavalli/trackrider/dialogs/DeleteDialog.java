package com.michaelcavalli.trackrider.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.michaelcavalli.trackrider.R;

/**
 * This dialog is used to delete tracks, track days, or sessions from the database
 */
public class DeleteDialog extends DialogFragment {
    private static final String LOG_TAG = DeleteDialog.class.getSimpleName();

    // The return interface activity
    DeleteCallback dataReturnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setMessage(R.string.delete_message)
                .setPositiveButton(R.string.OK_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Call the delete method in the return activity interface
                        dataReturnActivity.delete();
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing, delete cancelled
                    }
                });

        return builder.create();
    }

    // Interface that must be implemented by the attached activity
    public interface DeleteCallback {
        public void delete();
    }

    /**
     * Must make sure attached activity implements the interface
     * @param context attached activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            // Attached activity must implement the interface
            dataReturnActivity = (DeleteCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the DataReturnInterface");
        }
    }
}
