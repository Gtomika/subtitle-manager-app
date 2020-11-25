package com.gaspar.subtitlemanager.subtitle;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.conversion.Converter;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

/**
 * Async task that writes lines to the given file. On completion it shows a
 * prompt to the user.
 */
public class SaveTask extends AsyncTask<SubtitleModifierActivity, Void, SaveTask.SaveTaskResult> {

    private File f;

    private List<String> lines;

    public SaveTask(File f, List<String> lines) {
        this.f = f;
        this.lines = lines;
    }

    @Override
    protected SaveTaskResult doInBackground(SubtitleModifierActivity... activities) {
        SubtitleModifierActivity activity = activities[0];
        boolean success = Converter.saveToFile(f, lines); //perform file writing
        return new SaveTaskResult(success, activity);
    }

    @Override
    protected void onPostExecute(SaveTaskResult result) {
        Context context = ((AppCompatActivity)result.activity).getApplicationContext();
        result.activity.setUnsavedOperations(!result.success); //store that no unsaved files (IF save was successful)
        String promptText = result.success ? context.getString(R.string.file_saved)
                : context.getString(R.string.file_save_error);
        Snackbar.make(result.activity.getSubtitleListView(), promptText,
                Snackbar.LENGTH_LONG).show(); //show result to user
    }

    static class SaveTaskResult {
        private boolean success;
        private SubtitleModifierActivity activity;

        SaveTaskResult(boolean success, SubtitleModifierActivity activity) {
            this.success = success;
            this.activity = activity;
        }
    }
}
