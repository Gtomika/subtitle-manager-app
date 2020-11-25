package com.gaspar.subtitlemanager.conversion;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.preference.PreferenceManager;

import com.gaspar.subtitlemanager.AdManager;
import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.addremove.CreateComponentActivity;
import com.gaspar.subtitlemanager.settings.SettingsActivity;
import com.gaspar.subtitlemanager.subtitle.InvalidSubtitleException;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * This activity converts subtitle files between supported formats.
 */
public class ConvertActivity extends AppCompatActivity {

    private String saveFolderPath;

    /**
     * The extension of the selected file.
     */
    private String selectedFileExtension;

    /**
     * Shows the the enabled extensions allow the user to convert or not.
     */
    private boolean canConvert;

    /**
     * The parsed subtitle components of the file that needs to be converted. This is initialized
     * on a background thread.
     */
    private volatile Subtitle subtitle;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.convert);
        AdManager.displayAd(findViewById(R.id.adView));

        if(getIntent().getExtras() == null) finish(); //should not happen
        File selectedFile = (File)getIntent().getExtras().getSerializable("selectedFile");
        if(selectedFile==null) finish(); //should not happen
        parseSubtitleComponent(selectedFile); //parse subtitle in background
        assert selectedFile != null;
        selectedFileExtension = findExtension(selectedFile.getName());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultFolderPath = prefs.getString(SettingsActivity.DEF_FOLDER_PREF_NAME, SettingsActivity.NO_DEFAULT_FOLDER);
        if(!defaultFolderPath.equals(SettingsActivity.NO_DEFAULT_FOLDER)) { //if there is a default folder
            saveFolderPath = defaultFolderPath;
            TextView pathDisplayer = findViewById(R.id.saveFolderPathDisplayer);
            pathDisplayer.setText(saveFolderPath);
        }
        EditText fileNameInput = findViewById(R.id.fileNameInput);
        fileNameInput.setText(removeExtension(selectedFile.getName())); //same name by default
        fileNameInput.addTextChangedListener(CreateComponentActivity.createTextListener(fileNameInput));

        TextView convertPrompt = findViewById(R.id.selecteConvertExtensionText); //set text that shows current extension
        convertPrompt.setText(getString(R.string.convert_into, Extensions.findFullNameFor(selectedFileExtension)));
        initializeSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume the AdView.
        ((AdView)findViewById(R.id.adView)).resume();
    }

    @Override
    public void onPause() {
        // Pause the AdView.
        ((AdView)findViewById(R.id.adView)).pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the AdView.
        ((AdView)findViewById(R.id.adView)).destroy();
        super.onDestroy();
    }

    /**
     * Reads the subtitle object from the selected file on a background thread.
     */
    private void parseSubtitleComponent(File file) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Converter converter = Converter.findConverter(file); //create the appropriate file converter
            try {
                subtitle = converter.convert();
            } catch (InvalidSubtitleException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConvertActivity.this);
                builder.setMessage(ConvertActivity.this.getString(R.string.invalid_subtitle_file, file.getName()));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, (dialog, id) -> ConvertActivity.this.finish());
                builder.create().show();
            }
        });
    }

    /**
     * Removes the extension from the name of a file.
     */
    private String removeExtension(String fileName) {
        int extensionStart = fileName.lastIndexOf(".");
        return fileName.substring(0, extensionStart);
    }

    /**
     * @return The extension of the given file.
     * @throws IllegalArgumentException If there is no extension.
     */
    private String findExtension(String fileName) throws IllegalArgumentException {
        String[] splitResult = fileName.split("\\.");
        String extension = splitResult[splitResult.length-1];
        if(extension.equals("")) throw new IllegalArgumentException("No extension!"); //no extension
        return extension;
    }

    /**
     * Sets up the extension selector spinner.
     */
    private void initializeSpinner() {
        List<String> extensions =
                new ArrayList<>(Arrays.asList(SettingsActivity.getSelectedExtensions(this, false)));
        String extensionFullName = Extensions.findFullNameFor(selectedFileExtension);
        extensions.remove(extensionFullName); //remove the file's extension
        if(extensions.size() > 0) { //there are more enabled extensions
            Spinner spinner = findViewById(R.id.extensionSelectorSpinner);
            spinner.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item, extensions);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            canConvert = true;
        } else { //this was the only enabled extension
            TextView warningView = findViewById(R.id.convertWarningView);
            String text = getString(R.string.no_other_extensions, extensionFullName);
            warningView.setText(text);
            warningView.setVisibility(View.VISIBLE);
            canConvert = false;
            //constraints need to be updated
            ConstraintSet set = new ConstraintSet();
            ConstraintLayout layout = findViewById(R.id.convertRoot);
            set.clone(layout);
            set.connect(R.id.selectSaveFolderPrompt, ConstraintSet.TOP,
                    R.id.convertWarningView, ConstraintSet.BOTTOM);
            set.applyTo(layout);
        }
    }

    /**
     * Called when the file name info button is clicked.
     */
    public void fileNameInfoOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dont_enter_extension);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Called when the convert button is clicked.
     */
    public void convertFileOnClick(View v) {
        EditText fileNameInput = findViewById(R.id.fileNameInput);
        if(subtitle == null) return; //subtitle is not yet parsed, should not happen
        if(invalidFileName(fileNameInput)) return; //check if file name is ok
        if(!canConvert) { //no enabled extensions to convert to
            findViewById(R.id.selecteConvertExtensionText).setBackgroundResource(R.drawable.error_background);
            return;
        }
        if(saveFolderPath == null) { //check if save folder is ok
            TextView pathDisplayer = findViewById(R.id.saveFolderPathDisplayer);
            pathDisplayer.setBackgroundResource(R.drawable.error_background);
            Snackbar.make(v, R.string.must_select_save_folder, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Spinner extensionSpinner = findViewById(R.id.extensionSelectorSpinner);
        String fileName = fileNameInput.getText().toString();
        String extensionName = Extensions.findShortNameFor(extensionSpinner.getSelectedItem().toString());

        File convertedFile = new File(saveFolderPath + "/" + fileName + "." + extensionName);
        if(convertedFile.exists()) { //ask user for overwrite permission
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.file_exists);
            builder.setPositiveButton(R.string.overwrite, (dialog, id) -> {
                try {
                    boolean success = convertedFile.delete();
                    if(!success) throw new IOException("Failed to delete");
                    convertedFile.createNewFile();
                    convertFile(subtitle, convertedFile); //does the conversion on a background thread.
                } catch (Exception e) {
                    Snackbar.make(v, R.string.file_create_error, Snackbar.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
            builder.create().show();
        } else { //file does not exists
            try {
                Log.d("----------------", convertedFile.getAbsolutePath());
                convertedFile.createNewFile();
                convertFile(subtitle, convertedFile); //does the conversion on a background thread.
            } catch (Exception e) {
                Snackbar.make(v, R.string.file_create_error, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the lines of one subtitle file into another. Works in a background thread.
     *
     * @param convertedFile The file in what the converted lines will be written into.
     */
    private void convertFile(Subtitle subtitle, File convertedFile) {
        new ConverterTask(subtitle, convertedFile).execute(this);
    }

    /**
     * Called when the browse for save folder button is clicked.
     */
    public void selectSaveFolderOnClick(View v) {
        new ChooserDialog(ConvertActivity.this)
                .withFilter(true, false) //only folders
                .withResources(R.string.default_folder, R.string.ok, R.string.cancel) //button text
                .withChosenListener((s, file) -> {
                    TextView pathDisplayer = findViewById(R.id.saveFolderPathDisplayer);
                    pathDisplayer.setBackgroundResource(R.drawable.dark_background_black_border_rounded);
                    pathDisplayer.setText(s); //update displayed path
                    saveFolderPath = s; //save selected path
                })
                .withOnCancelListener(DialogInterface::cancel).build().show();
    }

    /**
     * Checks if the file name is valid.
     */
    private boolean invalidFileName(EditText fileNameInput) {
        String fileName = fileNameInput.getText().toString();
        if(fileName.equals("")) { //empty
            fileNameInput.setBackgroundResource(R.drawable.error_background);
            Snackbar.make(fileNameInput, R.string.must_select_file_name, Snackbar.LENGTH_SHORT).show();
            return true;
        } else if(fileName.contains(".")) { //extension also entered
            fileNameInput.setBackgroundResource(R.drawable.error_background);
            Snackbar.make(fileNameInput, R.string.dont_enter_extension, Snackbar.LENGTH_LONG).show();
            return true;
        }
        return false;
    }
}
