package com.michaelcavalli.trackrider;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * This class watches for changes in the EditTexts on the details page.  If anything changes, it
 * calls the RecordData method in the activity that implements the TextWatcherCallback interface.
 */
public class DetailsTextWatcher implements TextWatcher {
    private static final String LOG_TAG = DetailsTextWatcher.class.getSimpleName();

    public EditText editText;           // The EditText that changed
    TextWatcherCallback callBack;       // The callback activity
    String beforeString;                // The before string in the EditText
    String afterString;                 // The after string in the EditText

    /**
     * Constructor that gets a reference to the EditText and activity that should implement the
     * TextWatcherCallback
     * @param et EditText with the listener
     * @param c The activity that implements the TextWatcherCallback
     */
    public DetailsTextWatcher(EditText et, Context c){
        editText=et;
        try {
            callBack = (TextWatcherCallback) c;
        } catch (ClassCastException e){
            throw new ClassCastException(c.toString() + " must implement the TextWatcherCallback Interface");
        }
    }

    // Gets the text before it changed
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        beforeString = charSequence.toString();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Not used
    }

    // Gets the text after it changed
    @Override
    public void afterTextChanged(Editable editable) {
        afterString = editable.toString();
        if(afterString.equals(beforeString)){
            // Do nothing
        } else {
            // If the text changed, record the new text in the DB
            callBack.RecordData(editText);
        }
    }

    // The interface for the implementing activity
    public interface TextWatcherCallback {
        public void RecordData(EditText et);
    }
}
