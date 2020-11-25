package com.gaspar.subtitlemanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.gaspar.subtitlemanager.addremove.AddRemoveActivity;
import com.gaspar.subtitlemanager.addremove.CreateComponentActivity;
import com.gaspar.subtitlemanager.conversion.Extensions;
import com.gaspar.subtitlemanager.settings.SettingsActivity;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.IOException;

/**
 * This activity creates new subtitle files. Upon creation it redirects to an empty add/remove
 * activity.
 */
public class CreateNewFileActivity extends AppCompatActivity {

    /**
     * Path where the new file will be saved.
     */
    private String saveFolderPath;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.create_new_file);
        AdManager.displayAd(findViewById(R.id.adView));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultFolderPath = prefs.getString(SettingsActivity.DEF_FOLDER_PREF_NAME, SettingsActivity.NO_DEFAULT_FOLDER);
        if(!defaultFolderPath.equals(SettingsActivity.NO_DEFAULT_FOLDER)) { //if there is a default folder
            saveFolderPath = defaultFolderPath;
            TextView pathDisplayer = findViewById(R.id.saveFolderPathDisplayer);
            pathDisplayer.setText(saveFolderPath);
        }
        EditText fileNameInput = findViewById(R.id.fileNameInput);
        fileNameInput.addTextChangedListener(CreateComponentActivity.createTextListener(fileNameInput));
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
     * Sets up the extension selector spinner.
     */
    private void initializeSpinner() {
        Spinner spinner = findViewById(R.id.extensionSelectorSpinner);
        String[] selectedExtensions = SettingsActivity.getSelectedExtensions(this, false);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, selectedExtensions
                );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    /**
     * Called when the browse for save folder button is clicked.
     */
    public void selectSaveFolderOnClick(View v) {
        //check read permission
        PermissionsManager manager = new PermissionsManager(this);
        if(manager.doesntHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            manager.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    PermissionsManager.READ_REQUEST_CODE);
            return; //if no read perm, request it and return
        }
        new ChooserDialog(CreateNewFileActivity.this)
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
     * Called when the create file button is clicked.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createFileOnClick(View v) {
        if(saveFolderPath == null) { //no save folder selected
            TextView pathDisplayer = findViewById(R.id.saveFolderPathDisplayer);
            pathDisplayer.setBackgroundResource(R.drawable.error_background);
            Snackbar.make(v, R.string.must_select_save_folder, Snackbar.LENGTH_SHORT).show();
            return;
        }
        EditText fileNameInput = findViewById(R.id.fileNameInput);
        String fileName = fileNameInput.getText().toString();
        if(invalidFileName(fileName, fileNameInput)) return;  //invalid file name
        Spinner extensionSpinner = findViewById(R.id.extensionSelectorSpinner);
        String extensionName = Extensions.findShortNameFor(extensionSpinner.getSelectedItem().toString());
        File createdFile = new File(saveFolderPath + "/" + fileName + "." + extensionName);

        if(createdFile.exists()) { //ask user for overwrite permission
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.file_exists);
            builder.setPositiveButton(R.string.overwrite, (dialog, id) -> {
                try {
                    boolean success = createdFile.delete();
                    if(!success) throw new IOException("Failed to delete");
                    createdFile.createNewFile();
                    startAddRemoveActivity(createdFile);
                } catch (Exception e) {
                    Snackbar.make(v, R.string.file_create_error, Snackbar.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
            builder.create().show();
        } else { //file does not exists
            try {
                Log.d("----------------", createdFile.getAbsolutePath());
                createdFile.createNewFile();
                startAddRemoveActivity(createdFile);
            } catch (Exception e) {
                Snackbar.make(v, R.string.file_create_error, Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * Checks if the file name is valid.
     */
    private boolean invalidFileName(String fileName, View fileNameInput) {
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

    /**
     * Starts an add remove activity with an empty, newly create file.
     */
    private void startAddRemoveActivity(File createdFile) {
        Intent intent = new Intent(CreateNewFileActivity.this, AddRemoveActivity.class);
        intent.putExtra("selectedFile", createdFile); //pass selected file
        startActivity(intent);
        finish(); //this is no longer needed
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
     * Called when the user decides to accept permission or not.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PermissionsManager.READ_REQUEST_CODE) { //only care about read request
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //read perm granted, call folder selector method again.
                selectSaveFolderOnClick(findViewById(R.id.selectSaveFolderButton));
            } else { //denied
                Snackbar.make(findViewById(R.id.confirmRoot), R.string.need_read_permission,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
