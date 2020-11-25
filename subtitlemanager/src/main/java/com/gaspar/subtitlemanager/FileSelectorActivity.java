package com.gaspar.subtitlemanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gaspar.subtitlemanager.addremove.AddRemoveActivity;
import com.gaspar.subtitlemanager.adjusting.AdjusterActivity;
import com.gaspar.subtitlemanager.conversion.ConvertActivity;
import com.gaspar.subtitlemanager.settings.SettingsActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

//the main activity, also serves as the file selector activity.
public class FileSelectorActivity extends AppCompatActivity {

    //the file that is selected, null if nothing is selected.
    private File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(getApplicationContext()); //initialize java.time backport
        MobileAds.initialize(this, initializationStatus -> { }); //initialize ads SDK
        setContentView(R.layout.file_selector);
        Toolbar toolbar = findViewById(R.id.toolbar); //set toolbar
        setSupportActionBar(toolbar);
    }

    //when the browse button is clicked
    public void selectFileButtonOnClick(View v) {
        //check read permission
        PermissionsManager manager = new PermissionsManager(this);
        if(manager.doesntHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            manager.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    PermissionsManager.READ_REQUEST_CODE);
            return; //if no read perm, request it and return
        }
        final TextView pathDisplayerView = findViewById(R.id.locationDisplayerTextView);

        String[] filters = SettingsActivity.getSelectedExtensions(this, true);
        if(filters.length == 0) { //no point showing if nothing is enabled
            Snackbar.make(v, R.string.no_extension_selected, Snackbar.LENGTH_LONG).show();
            return;
        }
        ChooserDialog dialog = new ChooserDialog(FileSelectorActivity.this)
                .withFilter(false, filters)
                .withResources(R.string.select_prompt, R.string.ok, R.string.cancel) //button text
                .withChosenListener((s, file) -> {
                    selectedFile = file; //save file
                    pathDisplayerView.setText(s); //update path displayer
                    fadeInvisibleComponents(true); //fade in the additional buttons
                })
                .withOnCancelListener(DialogInterface::cancel);

        String folderPath = SettingsActivity.getUncertainFolderPath(getApplicationContext());
        if(folderPath.equals(SettingsActivity.NO_DEFAULT_FOLDER)) {
            dialog.build().show(); //no default folder, or was deleted
        } else {
            dialog.withStartFile(folderPath).build().show(); //valid default folder
        }
    }

    /**
     * Called when the reset selected file button was clicked.
     */
    public void resetSelectedFileOnClick(View v) {
        if(selectedFile == null) return; //no need to do anything in this case
        TextView pathDisplayer = findViewById(R.id.locationDisplayerTextView);
        pathDisplayer.setText(R.string.no_file_selected); //reset the text
        selectedFile = null; //reset the selected file
        fadeInvisibleComponents(false); //fade out additional buttons
    }

    /**
     * Called when the adjust button is clicked. This button is only visible when a file is selected.
     */
    public void adjustButtonOnClick(View v) {
        if(selectedFile == null) return; //this should not happen, as button is invisible in this case
        Intent intent = new Intent(FileSelectorActivity.this, AdjusterActivity.class);
        intent.putExtra("selectedFile", selectedFile); //pass selected file
        FileSelectorActivity.this.startActivity(intent);
    }

    /**
     * Called when the edit text button is clicked. This button is only visible when a file is selected.
     */
    public void editButtonOnClick(View v) {
        if(selectedFile == null) return; //this should not happen, as button is invisible in this case
        Intent intent = new Intent(FileSelectorActivity.this, TextEditActivity.class);
        intent.putExtra("selectedFile", selectedFile); //pass selected file
        FileSelectorActivity.this.startActivity(intent);
    }

    /**
     * Called when the add/remove components button is clicked.
     */
    public void addRemoveButtonOnClick(View v) {
        if(selectedFile == null) return; //this should not happen, as button is invisible in this case
        Intent intent = new Intent(FileSelectorActivity.this, AddRemoveActivity.class);
        intent.putExtra("selectedFile", selectedFile); //pass selected file
        FileSelectorActivity.this.startActivity(intent);
    }

    /**
     * Called when the convert button is clicked.
     */
    public void convertButtonOnClick(View v) {
        if(selectedFile == null) return; //this should not happen, as button is invisible in this case
        Intent intent = new Intent(FileSelectorActivity.this, ConvertActivity.class);
        intent.putExtra("selectedFile", selectedFile); //pass selected file
        FileSelectorActivity.this.startActivity(intent);
    }

    /**
     * Called when the create new file button is clicked.
     */
    public void createNewFile(View v) {
        //check write permission
        PermissionsManager manager = new PermissionsManager(this);
        if(manager.doesntHavePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            manager.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PermissionsManager.WRITE_REQUEST_CODE);
            return; //if no read perm, request it and return
        }
        String[] filters = SettingsActivity.getSelectedExtensions(this, true);
        if(filters.length == 0) { //no point showing if nothing is enabled
            Snackbar.make(v, R.string.no_extension_selected, Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(FileSelectorActivity.this, CreateNewFileActivity.class);
        FileSelectorActivity.this.startActivity(intent);
    }

    /**
     * Reveals or hides the separator, the adjust and the edit buttons when a file is selected.
     */
    public void fadeInvisibleComponents(boolean isFadeIn) {
        View fadeComponents = findViewById(R.id.fadeComponents);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                isFadeIn ? R.anim.fade_in : R.anim.fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
               if(isFadeIn) fadeComponents.setVisibility(View.VISIBLE); //only if fading in
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(!isFadeIn) fadeComponents.setVisibility(View.INVISIBLE); //only if fading out
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                //not needed
            }
        });
      fadeComponents.startAnimation(animation);
    }

    //create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //when toolbar menu item is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //to avoid error like the selected files extension is disabled
        if(selectedFile != null) {
            TextView pathDisplayer = findViewById(R.id.locationDisplayerTextView);
            pathDisplayer.setText(R.string.no_file_selected); //reset the text
            selectedFile = null; //reset the selected file
            View fadeComponents = findViewById(R.id.fadeComponents);
            fadeComponents.clearAnimation();
            fadeComponents.setVisibility(View.INVISIBLE); //hide additional buttons
        }
        Intent intent = new Intent(FileSelectorActivity.this, SettingsActivity.class);
        FileSelectorActivity.this.startActivity(intent);
        return true;
    }

    /**
     * Called when the user decides to accept permission or not.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PermissionsManager.READ_REQUEST_CODE) { //only care about read request
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //read perm granted, call file selector method again.
                selectFileButtonOnClick(findViewById(R.id.browseButton));
            } else { //denied
                Snackbar.make(findViewById(R.id.confirmRoot), R.string.need_read_permission,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
