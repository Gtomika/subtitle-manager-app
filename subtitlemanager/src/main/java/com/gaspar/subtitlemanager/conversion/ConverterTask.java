package com.gaspar.subtitlemanager.conversion;

import android.os.AsyncTask;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

/**
 * Background task that converts a subtitle file into a different format.
 */
public class ConverterTask extends AsyncTask<ConvertActivity, Void, ConvertActivity> {

    /**
     * The files that are used in the conversion. The files received in the constructor are existing
     * files, the second one is empty.
     */
    private File convertedFile;

    /**
     * The parsed subtitle components of the file that needs to be converted.
     */
    private Subtitle subtitle;

    ConverterTask(Subtitle subtitle, File convertedFile) {
        this.subtitle = subtitle;
        this.convertedFile = convertedFile;
    }

    @Override
    protected ConvertActivity doInBackground(ConvertActivity... convertActivities) {
        ConvertActivity activity = convertActivities[0];
        Converter converter = Converter.findConverter(convertedFile); //converter of the newly created file.
        Converter.saveToFile(convertedFile, converter.toLines(subtitle)); //write appropriate lines to file
        return activity;
    }

    @Override
    protected void onPostExecute(ConvertActivity activity) {
        Snackbar.make(activity.findViewById(R.id.convertFileButton),
                R.string.convert_successful, Snackbar.LENGTH_SHORT).show();
    }
}
