package com.gaspar.subtitlemanager.adjusting;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gaspar.subtitlemanager.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Background task that performs the timing adjusting. After that it updates the list view
 * showing the subtitles and prompts the user.
 */
public class PerformAdjustTask extends AsyncTask<AdjusterActivity, Void, AdjusterActivity> {

    /**
     * Operation constant that determines if speed up or delay is performed.
     */
    private AdjusterActivity.Operation operation;

    /**
     * The amount if millisecs to delay or speed up.
     */
    private int millisecs;

    PerformAdjustTask(AdjusterActivity.Operation operation, int millisecs) {
        this.operation = operation;
        this.millisecs = millisecs;
    }

    @Override
    protected AdjusterActivity doInBackground(AdjusterActivity... adjusterActivities) {
        AdjusterActivity activity = adjusterActivities[0];
        //perform adjust, this modifies the activity's subtitle object
        new Adjuster(activity.getSubtitle()).adjust(operation,millisecs);
        return activity;
    }

    @Override
    protected void onPostExecute(AdjusterActivity activity) {
        ListView displayView = activity.findViewById(R.id.subtitleDisplayerView); //update list view
        ((ArrayAdapter)displayView.getAdapter()).notifyDataSetChanged(); //notify the adapter
        String finalMessage;
        if(operation == AdjusterActivity.Operation.SPEED_UP) {
            finalMessage = activity.getString(R.string.adjust_operation_performed,
                    activity.getString(R.string.speed_up));
        } else { //delay
            finalMessage = activity.getString(R.string.adjust_operation_performed,
                    activity.getString(R.string.delay));
        }
        Snackbar.make(activity.findViewById(R.id.performAdjustButton), finalMessage,
                Snackbar.LENGTH_LONG).show(); //prompt user
    }
}
