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
 * Created by silen_000 on 8/7/2016.
 */
public class DeleteDialog extends DialogFragment {
    private static final String LOG_TAG = DeleteDialog.class.getSimpleName();

    DeleteCallback dataReturnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setMessage(R.string.delete_message)
                .setPositiveButton(R.string.OK_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.v(LOG_TAG, "OK TO DELETE");
                        dataReturnActivity.delete();
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }

    public interface DeleteCallback {
        public void delete();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            dataReturnActivity = (DeleteCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the DataReturnInterface");
        }
    }
}
