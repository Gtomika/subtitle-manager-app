package com.gaspar.subtitlemanager.settings;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.gaspar.subtitlemanager.AdManager;
import com.gaspar.subtitlemanager.PermissionsManager;
import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.conversion.Extensions;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity of settings screen.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Constant of no default folder
     */
    public static final String NO_DEFAULT_FOLDER = "NO_DEFAULT_FOLDER" ;

    /**
     * Name of the def folder path field in the preferences.
     */
    public static final String DEF_FOLDER_PREF_NAME = "defaultFolderPath";

    /**
     * Shared preferences object.
     */
    private SharedPreferences prefs;

    /**
     * Extension togglers.
     */
    public List<ExtensionToggler> extensionTogglers = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        AdManager.displayAd(findViewById(R.id.adView));
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int counter = 0;
        for(String extensionName: Extensions.getSupportedExtensionNames()) {
            if(!prefs.contains(extensionName)) { //for first launch of app
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(extensionName,true).apply(); //enabled by default
            }
            boolean enabled = prefs.getBoolean(extensionName, false);
            extensionTogglers.add(new ExtensionToggler(this, extensionName, enabled));
            if(!enabled) counter++;
        }
        ListView extensionTogglerView = findViewById(R.id.extensionTogglerView); //fill list view
        extensionTogglerView.setAdapter(new ExtensionTogglerAdapter(extensionTogglers));
        if(counter == extensionTogglers.size()) findViewById(R.id.noExtensionWarningView).setVisibility(View.VISIBLE);

        if(!prefs.contains(DEF_FOLDER_PREF_NAME)) { //first time launching app
            prefs.edit().putString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER).apply();
        }
        TextView defFolderPathView = findViewById(R.id.folderPathTextView); //display default folder path.
        String defaultFolderPath = prefs.getString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER);
        if(defaultFolderPath.equals(NO_DEFAULT_FOLDER)) {
            defFolderPathView.setText(R.string.no_folder_selected);
        } else {
            defFolderPathView.setText(defaultFolderPath);
        }
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
     * Called when the enabled extensions info button is clicked.
     */
    public void enabledExtensionsInfoOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enabled_extensions_info);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Called when the default folder info button is clicked.
     */
    public void defaultFolderInfoOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.default_folder_info);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Called when the browse folder button is clicked.
     */
    public void browseFolderButtonOnClick(View v) {
        //check read permission
        PermissionsManager manager = new PermissionsManager(this);
        if(manager.doesntHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            manager.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    PermissionsManager.READ_REQUEST_CODE);
            return; //if no read perm, request it and return
        }
        new ChooserDialog(SettingsActivity.this)
                .withFilter(true, false) //only folders
                .withResources(R.string.default_folder, R.string.ok, R.string.cancel) //button text
                .withChosenListener((s, file) -> {
                    TextView pathDisplayer = findViewById(R.id.folderPathTextView);
                    pathDisplayer.setText(s); //update displayed path
                    prefs.edit().putString(DEF_FOLDER_PREF_NAME, s).apply();
                })
                .withOnCancelListener(DialogInterface::cancel).build().show();
    }

    /**
     * Called when the delete default folder button is clicked.
     */
    public void deleteDefaultFolder(View v) {
        Snackbar.make(findViewById(R.id.settingsRoot), R.string.default_folder_was_reset,
                Snackbar.LENGTH_SHORT).show(); //prompt
        if(prefs.getString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER)
                .equals(NO_DEFAULT_FOLDER)) return; //no need to do anything
        TextView pathView = findViewById(R.id.folderPathTextView);
        pathView.setText(R.string.no_folder_selected); //reset displayed path
       prefs.edit().putString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER).apply();
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
                browseFolderButtonOnClick(findViewById(R.id.browseFolderButton));
            } else { //denied
                Snackbar.make(findViewById(R.id.folderPathTextView),
                        R.string.need_read_permission, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Returns the default folder path but only if it exists. If the folder
     * was deleted it also updates the database and the global variable.
     */
    public static String getUncertainFolderPath(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String folderPath = prefs.getString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER);
        if(!folderPath.equals(NO_DEFAULT_FOLDER) && !new File(folderPath).exists()) { //folder was deleted
            prefs.edit().putString(DEF_FOLDER_PREF_NAME, NO_DEFAULT_FOLDER).apply(); //update prefs
            return NO_DEFAULT_FOLDER;
        }
        return folderPath;
    }

    /**
     * Reads the selected extensions from the preferences.
     *
     * @param shortName Set to true if the array will be used to navigate in the file system so only
     * extension names will be returned. If false the "full" names will be returned, like "SRT (SubRip)"
     */
    public static String[] getSelectedExtensions(Context context, boolean shortName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> list = new ArrayList<>();
        for(Extensions.Extension extension: Extensions.Extension.values()) {
            boolean b;
            if(prefs.contains(extension.extensionName())) {
                b = prefs.getBoolean(extension.extensionName(), false);
            } else { //not found, first run, put it in with default value
                b = true;
                prefs.edit().putBoolean(extension.extensionName(), true).apply();
            }
            if(b) list.add(shortName ? extension.shortExtensionName() : extension.extensionName());
        }
        return list.toArray(new String[]{});
    }
}
