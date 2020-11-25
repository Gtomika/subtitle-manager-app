package com.gaspar.subtitlemanager.addremove;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.AdManager;
import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.conversion.Converter;
import com.gaspar.subtitlemanager.subtitle.SaveTask;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;
import com.gaspar.subtitlemanager.subtitle.SubtitleModifierActivity;
import com.google.android.gms.ads.AdView;

import org.threeten.bp.LocalTime;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * The activity which can add and remove components from a subtitle file.
 */
public class AddRemoveActivity extends AppCompatActivity implements SubtitleModifierActivity {

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

    /**
     * Stores if the app should ask for confirmation before deleting a component. True on activity start.
     */
    private boolean askForConfirmation;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.add_remove);
        AdManager.displayAd(findViewById(R.id.adView));
        selectedFile = (File)getIntent().getSerializableExtra("selectedFile"); //retrieve file
        unsavedOperations = false;
        askForConfirmation = true;
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
     * Initialized the UI components of this activity.
     */
    private void setUpUI() {
        TextView fileNameTextView = findViewById(R.id.fileNameTextView); //filename text view
        fileNameTextView.setText(selectedFile.getName());

        View firstAddView = findViewById(R.id.addFirstComponent);
        TextView firstAddText = firstAddView.findViewById(R.id.addComponentPrompt);
        firstAddText.setText(R.string.add_new_component_to_beginning); //first one gets custom text

        ImageButton firstAddButton = firstAddView.findViewById(R.id.addNewComponentButton);
        firstAddButton.setOnClickListener(view -> { //first add new component is added here
            LocalTime nextStartTime = subtitle.getComponents().size() > 1 ? subtitle.getComponents().get(1)
                    .getStartTime() : null;
            startActivityForComponent(0, null, nextStartTime);
        });
        new FillAddRemoveTask(selectedFile).execute(this); //fill the list view with the AddRemoveAdapter
    }

    /**
     * Starts an activity that is used to create a new subtitle component.
     *
     * @param index Where the newly created component is inserted into the subtitle object and the list view.
     * @param prevEndTime End time of previous component.
     * @param nextStartTime Start time of next component.
     */
    public void startActivityForComponent(int index, LocalTime prevEndTime, LocalTime nextStartTime) {
        Intent intent = new Intent(this, CreateComponentActivity.class);
        intent.putExtra(CreateComponentActivity.INDEX_IDENTIFIER, index); //pass index
        intent.putExtra(CreateComponentActivity.PREV_END_IDENTIFIER, prevEndTime); //pass neighbour components data
        intent.putExtra(CreateComponentActivity.NEXT_START_IDENTIFIER, nextStartTime);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) { //create component activity ended
            if(resultCode == Activity.RESULT_OK){
                if(data.getExtras()==null) return; //should not happen
                unsavedOperations = true; //start unsaved
                SubtitleComponent component = (SubtitleComponent) data.getExtras().
                        getSerializable(CreateComponentActivity.RESULT_IDENTIFIER);
                int index = data.getIntExtra(CreateComponentActivity.INDEX_IDENTIFIER, 0); //read index
                addComponent(component, index);
            }
        } //do nothing when cancelled.
    }

    /**
     * Adds the given component to the subtitle object and updates the list view.
     *
     * @throws IllegalArgumentException Invalid index.
     */
    public void addComponent(SubtitleComponent component, int index) throws IllegalArgumentException {
        //check when not empty
        if(!subtitle.getComponents().isEmpty() && (index<0 || index>subtitle.getComponents().size()-1)) {
            throw new IllegalArgumentException("Invalid index for component insertion!");
        } else if(subtitle.getComponents().isEmpty() && index<0) { //check when empty
            throw new IllegalArgumentException("Invalid index for component insertion!");
        }
        subtitle.getComponents().add(index, component);
        ListView subtitleDisplay = getSubtitleListView();
        ListAdapter adapter = subtitleDisplay.getAdapter();
        ((ArrayAdapter)adapter).notifyDataSetChanged(); //update list
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
     * Called when the save button is clicked.
     */
    public void saveButtonOnClick(View v) {
        //this will write to file and show prompt as well
        new SaveTask(selectedFile, converter.toLines(subtitle)).execute(this);
    }

    /**
     * Creates a listener that deletes the tapped component from the list.
     */
    @Override
    public View.OnClickListener createComponentListener(SubtitleComponent component) {
        return view -> {
            if(askForConfirmation) { //must confirm
                View confirmView = View.inflate(getApplicationContext(), R.layout.confirm_delete, null); //prepare view
                CheckBox dontAskAgainCheckbox = confirmView.findViewById(R.id.askMeAgainCheckbox);

                AlertDialog.Builder builder = new AlertDialog.Builder(this); //build dialog
                builder.setView(confirmView);
                builder.setPositiveButton(R.string.delete, (dialog, id) -> {
                    askForConfirmation = !dontAskAgainCheckbox.isChecked(); //if checked the no need for confirm anymore
                    dialog.dismiss();
                    deleteComponent(component);
                });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                    askForConfirmation = !dontAskAgainCheckbox.isChecked(); //if checked the no need for confirm anymore
                    dialog.dismiss();
                });
                builder.setCancelable(false); //needed for modality
                builder.create().show();
            } else {
                deleteComponent(component);
            }
        };
    }

    private void deleteComponent(SubtitleComponent component) {
        unsavedOperations = true;
        subtitle.getComponents().remove(component); //delete component
        ListView subtitleDisplay = getSubtitleListView();
        ListAdapter adapter = subtitleDisplay.getAdapter();
        ((ArrayAdapter)adapter).notifyDataSetChanged(); //update list
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
