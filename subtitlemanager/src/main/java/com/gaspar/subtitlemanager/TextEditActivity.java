package com.gaspar.subtitlemanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.conversion.Converter;
import com.gaspar.subtitlemanager.subtitle.SaveTask;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;
import com.gaspar.subtitlemanager.subtitle.SubtitleFillerTask;
import com.gaspar.subtitlemanager.subtitle.SubtitleModifierActivity;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * The activity that can modify that texts of a subtitle file.
 */
public class TextEditActivity extends AppCompatActivity implements SubtitleModifierActivity {

    /**
     * The file that this activity will modify.
     */
    private File selectedFile;

    /**
     * The subtitle object that this activity displays, modifies and saves.
     */
    private volatile Subtitle subtitle;

    /**
     * The converter object that that handles converting to abstract subtitle format.
     */
    private volatile Converter converter;

    /**
     * Stores if there are unsaved operations.
     */
    private volatile boolean unsavedOperations;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.edit_text);
        AdManager.displayAd(findViewById(R.id.adView));
        selectedFile = (File)getIntent().getSerializableExtra("selectedFile"); //retrieve file
        unsavedOperations = false;
        setUpUI(); //fill UI text views and preview
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
     * Prepares the user interface according to to the selected file.
     */
    private void setUpUI() {
        TextView fileNameTextView = findViewById(R.id.fileNameTextView); //filename text view
        fileNameTextView.setText(selectedFile.getName());

        //THIS SETS THE SUBTITLE INSTANCE VARIABLE AS WELL
        new SubtitleFillerTask(selectedFile).execute(this); //fill subtitle preview on background
    }

    /**
     * Called when the user presses the info button.
     */
    public void infoButtonOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.edit_text_info);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss()); //ok
        builder.create().show();
    }

    /**
     * Called when the save button is clicked.
     */
    public void saveButtonOnClick(View v) {
        //this will write to file and show prompt as well
        new SaveTask(selectedFile, converter.toLines(subtitle)).execute(this);
    }

    /**
     * Called when the user clicks the back button.
     */
    @Override
    public void onBackPressed() {
        if(unsavedOperations) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changed);
            builder.setPositiveButton(R.string.save, (dialog, id) -> { //save button
                Executors.newSingleThreadExecutor().execute(() ->
                        Converter.saveToFile(selectedFile, converter.toLines(subtitle)));
                finish();
            });
            builder.setNegativeButton(R.string.dont_save, (dialog, id) -> { //no save button
                finish();
            });
            builder.create().show();
        } else { //nothing to save
            super.onBackPressed();
        }
    }

    /**
     * Called when the user clicks the toolbar back button. Does the same as the android back button.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //no need to check for any other buttons, as there is only the back button on the toolbar here
        onBackPressed();
        return true;
    }

    /**
     * Reference to the currently active dialog.
     */
    private AlertDialog dialog;

    /**
     * A listener that opens a dialog when a component is clicked. The dialog contains text editing
     * interface.
     */
    @Override
    public View.OnClickListener createComponentListener(SubtitleComponent component) {
        return view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.edit_text_label);
            builder.setView(createEditTextInterface(component));
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss()); //cancel button
            dialog = builder.create();
            dialog.show();
        };
    }

    /**
     * Creates an edit interface that is shown when a component is clicked.
     */
    private View createEditTextInterface(SubtitleComponent component) {
        View view = View.inflate(getApplicationContext(), R.layout.edit_text_individual_component, null);
        EditText editArea = view.findViewById(R.id.editTextArea);
        editArea.setText(component.getText()); //text is the same at the start
        Button editButton = view.findViewById(R.id.performEditTextButton);
        editButton.setOnClickListener(view1 -> {
            String text = editArea.getText().toString().trim();
            if(text.equals("")) {
                Snackbar.make(view, R.string.invalid_text, Snackbar.LENGTH_SHORT).show();
                return;
            }
            setUnsavedOperations(true); //register unsaved operation
            component.setText(text); //update component
            dialog.dismiss();

            Snackbar.make(findViewById(R.id.subtitleDisplayerView),
                   R.string.edit_text_performed , Snackbar.LENGTH_LONG).show(); //prompt user
            ListView list = findViewById(R.id.subtitleDisplayerView);
            ((ArrayAdapter)list.getAdapter()).notifyDataSetChanged(); //update subtitle view
        });
        return view;
    }

    @Override
    public ListView getSubtitleListView() {
        return findViewById(R.id.subtitleDisplayerView);
    }

    @Override
    public Subtitle getSubtitle() {
        return subtitle;
    }

    @Override
    public void setSubtitle(Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public void setUnsavedOperations(boolean unsavedOperations) {
        this.unsavedOperations = unsavedOperations;
    }
}
