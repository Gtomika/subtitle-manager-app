package com.gaspar.subtitlemanager.addremove;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.conversion.Converter;
import com.gaspar.subtitlemanager.subtitle.InvalidSubtitleException;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleModifierActivity;

import java.io.File;

/**
 * A background task that fills a list view with subtitle components that can be deleted or added.
 */
public class FillAddRemoveTask extends AsyncTask<SubtitleModifierActivity, Void, SubtitleModifierActivity> {

    private File file;

    public FillAddRemoveTask(File file) {
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
        return activity;
    }

    @Override
    protected void onPostExecute(SubtitleModifierActivity activity) {
        Context context = ((AppCompatActivity)activity).getApplicationContext();
        if(activity.getSubtitle() == null) { //subtitle file could not be parsed.
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.invalid_subtitle_file, file.getName()));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> ((AppCompatActivity)activity).finish());
            builder.create().show();
            return;
        }
        ListView displayView = activity.getSubtitleListView();
        displayView.setAdapter(new AddRemoveAdapter(activity, activity.getSubtitle().getComponents())); //set the adapter
    }
}
