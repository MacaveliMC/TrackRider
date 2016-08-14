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
 * Created by silen_000 on 8/11/2016.
 */
public class AddLapTimeDialog extends DialogFragment {
    private static final String LOG_TAG = AddLapTimeDialog.class.getSimpleName();

    ReturnLapTimeInterface returnLapTimeActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        EditText new_lap_time;

        builder.setView(inflater.inflate(R.layout.add_lap_time_dialog, null))

                .setPositiveButton(R.string.OK_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        returnLapTimeActivity.OnDialogPositiveClick(AddLapTimeDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            returnLapTimeActivity = (ReturnLapTimeInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " MUST IMPLEMENT THE RETURN LAP TIME INTERFACE");
        }
    }

    public interface ReturnLapTimeInterface{
        public void OnDialogPositiveClick(DialogFragment dialog);
        public void OnDialogNegativeClick(DialogFragment dialog);
    }
}
