package com.michaelcavalli.trackrider;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by silen_000 on 8/11/2016.
 */
public class DetailsTextWatcher implements TextWatcher {
    private static final String LOG_TAG = DetailsTextWatcher.class.getSimpleName();

    public EditText editText;
    TextWatcherCallback callBack;
    String beforeString;
    String afterString;

    public DetailsTextWatcher(EditText et, Context c){
        editText=et;
        callBack = (TextWatcherCallback) c;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        beforeString = charSequence.toString();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //Log.v(LOG_TAG, "Int i: " + i + " Int i1: " + i1 + " Int i2: " + i2);
        //if((i+i1+i2) != 0)
        //    callBack.RecordData(editText);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        afterString = editable.toString();
        if(afterString.equals(beforeString)){
        } else {
            callBack.RecordData(editText);
        }
    }

    public interface TextWatcherCallback {
        public void RecordData(EditText et);
    }
}
