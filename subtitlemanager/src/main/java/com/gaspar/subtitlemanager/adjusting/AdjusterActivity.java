package com.gaspar.subtitlemanager.adjusting;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
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
import com.gaspar.subtitlemanager.subtitle.SubtitleFillerTask;
import com.gaspar.subtitlemanager.subtitle.SubtitleModifierActivity;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * The activity of the subtitle timing adjuster function.
 */
public class AdjusterActivity extends AppCompatActivity implements SubtitleModifierActivity {

    /**
     * The file that this activity will modify.
     */
    private File selectedFile;

    enum Operation { SPEED_UP, DELAY }

    /**
     * The subtitle object that this activity displays, modifies and saves.
     */
    private volatile Subtitle subtitle;

    /**
     * The converter object that that handles converting to abstract subtitle format.
     */
    private volatile Converter converter;

    /**
     * Contains info about what operation must be done.
     */
    private Operation toDo = Operation.SPEED_UP;

    /**
     * Stores if there are unsaved operations.
     */
    private volatile boolean unsavedOperations;

   @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.adjuster);
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
     * Sets the texts for the user interface from the given subtitle file
     */
   private void setUpUI() {
       AdManager.displayAd(findViewById(R.id.adView)); //banner ad
       TextView fileNameTextView = findViewById(R.id.fileNameTextView); //filename text view
       fileNameTextView.setText(selectedFile.getName());

       Switch delayOrSpeedUp = findViewById(R.id.delayOrSpeedUpSwitch);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //operation with customization
           delayOrSpeedUp.setShowText(true);
           delayOrSpeedUp.setThumbResource(R.drawable.app_switch_thumb);
       }
       delayOrSpeedUp.setOnCheckedChangeListener((compoundButton, b) -> {//add listener to the operation selector
            toDo = b ? Operation.DELAY : Operation.SPEED_UP;
       });

       //THIS SETS THE SUBTITLE INSTANCE VARIABLE AS WELL
       new SubtitleFillerTask(selectedFile).execute(this); //fill subtitle preview on background
   }

    /**
     * Called when the perform operation button is clicked.
     */
    public void performAdjustOperation(View v) {
        TextView secsView = findViewById(R.id.secondsEditText);
        TextView millisecsView = findViewById(R.id.millisecondsEditText);
        int millisecs;
        try {
            millisecs = 1000 * Integer.parseInt(secsView.getText().toString())
                    + Integer.parseInt(millisecsView.getText().toString());
        } catch (NumberFormatException e) { //should not happen as user can only enter numbers...
            Snackbar.make(findViewById(R.id.performAdjustButton), R.string.invalid_timings,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        unsavedOperations = true; //start unsaved operation
        new PerformAdjustTask(toDo, millisecs).execute(this);
    }

    /**
     * Called when the save button is clicked.
     */
   public void saveButtonOnClick(View v) {
       //this will write to file and show prompt as well
        new SaveTask(selectedFile, converter.toLines(subtitle)).execute(this);
   }

    /**
     * Called when the user presses the info button.
     */
   public void infoButtonOnClick(View v) {
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle(R.string.action_info);
       builder.setView(View.inflate(getApplicationContext(), R.layout.adjuster_info, null));
       builder.setPositiveButton(R.string.ok, (dialog, id) -> { }); //ok
       builder.create().show();
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
     * Reference to the currently opened dialog.
     */
    private AlertDialog dialog;

    /**
     * @return An onClickListener that will be called when the user taps on an a subtitle component view.
     */
    @Override
    public View.OnClickListener createComponentListener(SubtitleComponent component)  {
        return view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if(component.getStartTime()==null || component.getEndTime()==null) { //invalid component
                builder.setMessage(R.string.adjuster_info_invalid);
            } else {
                builder.setTitle(R.string.adjuster_label);
                builder.setView(createAdjusterDialogView(component));
            }
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss()); //cancel button
            dialog = builder.create();
            dialog.show();
        };
    }

    /**
     * Creates the view that shows in the dialog when an individual subtitle component is clicked.
     */
    private View createAdjusterDialogView(SubtitleComponent component) {
        View view = View.inflate(getApplicationContext(), R.layout.adjust_individual_component, null);
        Button adjustButton = view.findViewById(R.id.performAdjustButton);
        adjustButton.setOnClickListener(view1 -> {
            TextView secsView = view.findViewById(R.id.secondsEditText);
            TextView millisecsView = view.findViewById(R.id.millisecondsEditText);
            int millisecs;
            try {
                millisecs = 1000 * Integer.parseInt(secsView.getText().toString())
                        + Integer.parseInt(millisecsView.getText().toString());
            } catch (NumberFormatException e) { //only happens when nothing is entered.
                secsView.setBackgroundResource(R.drawable.error_background);
                millisecsView.setBackgroundResource(R.drawable.error_background);
                return;
            }
            Switch selectOperationSwitch = view.findViewById(R.id.delayOrSpeedUpSwitch);
            Operation toDo =  selectOperationSwitch.isChecked() ? Operation.DELAY : Operation.SPEED_UP;
            setUnsavedOperations(true); //start unsaved operation
            new SimpleAdjuster(component).adjust(toDo, millisecs); //perform adjust

            dialog.dismiss(); //close dialog here, prompt will show outside

            String finalMessage;
            if(toDo == AdjusterActivity.Operation.SPEED_UP) {
                finalMessage = getString(R.string.adjust_operation_performed, getString(R.string.speed_up));
            } else { //delay
                finalMessage = getString(R.string.adjust_operation_performed, getString(R.string.delay));
            }
            Snackbar.make(findViewById(R.id.subtitleDisplayerView),
                    finalMessage, Snackbar.LENGTH_LONG).show(); //prompt user

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
