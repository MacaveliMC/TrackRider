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
 * Created by silen_000 on 10/17/2016.
 */

public class SelectionDialog extends DialogFragment {
    private static final String LOG_TAG = SelectionDialog.class.getSimpleName();

    // The return interface activity
    SelectionDialog.SelectCallback dataReturnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();



        builder.setItems(R.array.select_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0)
                    dataReturnActivity.changeName();
                if(i == 1)
                    dataReturnActivity.delete();
            }
        });
        return builder.create();
    }

    public interface SelectCallback {
        void delete();
        void changeName();
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
            dataReturnActivity = (SelectionDialog.SelectCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the DataReturnInterface");
        }
    }
}
