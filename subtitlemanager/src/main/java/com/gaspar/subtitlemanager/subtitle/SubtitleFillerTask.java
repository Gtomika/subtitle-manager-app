package com.gaspar.subtitlemanager.subtitle;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.conversion.Converter;

import java.io.File;

/**
 * Fills the subtitle preview list in the adjuster activity.
 */
public class SubtitleFillerTask extends AsyncTask<SubtitleModifierActivity, Void, SubtitleModifierActivity> {

    private File file;

    public SubtitleFillerTask(File file) {
        this.file = file;
    }

    @Override
    protected SubtitleModifierActivity doInBackground(SubtitleModifierActivity... activities) {
        SubtitleModifierActivity activity = activities[0];
        Converter converter = Converter.findConverter(file); //create the appropriate file converter
        activity.setConverter(converter); //save it
        Subtitle subtitle;
        try {
            subtitle = converter.convert(); //create the subtitle file
        } catch (InvalidSubtitleException e) {
            subtitle = null; //set the subtitle object as null if invalid sub file is detected
        }
        activity.setSubtitle(subtitle); //save subtitle
        return activity; //pass to post execute
    }

    @Override
    protected void onPostExecute(SubtitleModifierActivity activity) {
        Context context = ((AppCompatActivity)activity).getApplicationContext();
        if(activity.getSubtitle() == null) { //subtitle file could not be parsed.
            AlertDialog.Builder builder = new AlertDialog.Builder((AppCompatActivity)activity);
            builder.setMessage(context.getString(R.string.invalid_subtitle_file, file.getName()));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> ((AppCompatActivity)activity).finish());
            builder.create().show();
            return;
        }
        ListView displayView = activity.getSubtitleListView();
        displayView.setAdapter(new SubtitleAdapter(activity, activity.getSubtitle()
                .getComponents())); //set the adapter
    }
}
