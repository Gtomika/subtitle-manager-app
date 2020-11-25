package com.gaspar.subtitlemanager.addremove;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.Utils;
import com.gaspar.subtitlemanager.conversion.Formatters;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;
import com.google.android.material.snackbar.Snackbar;

import org.threeten.bp.LocalTime;

/**
 * An activity that is used to create a subtitle component. It returns this component on completion.
 */
public class CreateComponentActivity extends AppCompatActivity {

    //identifiers for the intent extras
    public static final String RESULT_IDENTIFIER = "resultComponent";
    public static final String INDEX_IDENTIFIER = "index";
    public static final String PREV_END_IDENTIFIER = "prevEndTime";
    public static final String NEXT_START_IDENTIFIER = "newStartTime";

    /**
     * Index where this activity creates the new component.
     */
    private int index;

    /**
     * Data about the neighbour component timings
     */
    @Nullable
    private LocalTime previousComponentEnd, nextComponentStart;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if(getIntent().getExtras()==null) finish(); //should not happen
        index = getIntent().getIntExtra(INDEX_IDENTIFIER, 0); //read out index
        previousComponentEnd = (LocalTime)getIntent().getExtras().getSerializable(PREV_END_IDENTIFIER);
        nextComponentStart = (LocalTime)getIntent().getExtras().getSerializable(NEXT_START_IDENTIFIER);
        setContentView(R.layout.create_new_component);
        addTextChangeListeners();
    }

    /**
     * Adds the listeners to all edit texts, these listeners will change the background back to normal.
     */
    private void addTextChangeListeners() {
        EditText textInput = findViewById(R.id.subtitleTextInput);
        textInput.addTextChangedListener(createTextListener(textInput));

        View view = findViewById(R.id.fromTimeView);
        EditText timeInput = view.findViewById(R.id.hoursInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.minutesInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.secondsInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.millisecondsInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));

        view = findViewById(R.id.toTimeView);
        timeInput = view.findViewById(R.id.hoursInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.minutesInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.secondsInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
        timeInput = view.findViewById(R.id.millisecondsInput);
        timeInput.addTextChangedListener(createTextListener(timeInput));
    }

    /**
     * Called when the create component button is clicked. This finishes the activity if a subtitle
     * component can be created.
     */
    public void createComponentOnClick(View view) {
        String text = parseTextInput(); //get text
        if(text == null) return;
        LocalTime startTime = parseTimeInput(findViewById(R.id.fromTimeView)); //get timings
        LocalTime endTime = parseTimeInput(findViewById(R.id.toTimeView));
        if(startTime == null || endTime == null) {
            Snackbar.make(view, R.string.invalid_timings, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(incorrectTimings(startTime, endTime)) return; //shows info dialogs as well
        SubtitleComponent component = SubtitleComponent.builder()
                .withText(text)
                .withStartTime(startTime)
                .withEndTime(endTime).build();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_IDENTIFIER, component); //put result component in
        resultIntent.putExtra(INDEX_IDENTIFIER, index); //put index back in
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Parses the subtitle text string. Sets the background red where there was an error
     * parsing.
     *
     * @return The string, or null if the string was not a valid subtitle text.
     */
    private String parseTextInput() {
        EditText textInput = findViewById(R.id.subtitleTextInput);
        String text =  textInput.getText().toString();
        if(text.equals("")) {
            textInput.setBackgroundResource(R.drawable.error_background);
            Snackbar.make(textInput, R.string.invalid_text, Snackbar.LENGTH_SHORT).show();
            return null;
        }
        return text;
    }

    /**
     * Parses a time object from the input fields. Sets the background red where there was an error
     * parsing.
     *
     * @return null if there is an error parsing, otherwise the time object.
     */
    private LocalTime parseTimeInput(View timeInputView) {
        int hours, mins, secs, millisecs;
        EditText hoursInput = timeInputView.findViewById(R.id.hoursInput);
        try {
            hours = Integer.parseInt(hoursInput.getText().toString());
            if(hours>23) throw new Exception();
        } catch (Exception e) {
            hoursInput.setBackgroundResource(R.drawable.error_background);
            return null;
        }
        EditText minsInput = timeInputView.findViewById(R.id.minutesInput);
        try {
            mins = Integer.parseInt(minsInput.getText().toString());
            if(mins>59) throw new Exception();
        } catch (Exception e) {
            minsInput.setBackgroundResource(R.drawable.error_background);
            return null;
        }
        EditText secsInput = timeInputView.findViewById(R.id.secondsInput);
        try {
            secs = Integer.parseInt(secsInput.getText().toString());
            if(secs>59) throw new Exception();
        } catch (Exception e) {
            secsInput.setBackgroundResource(R.drawable.error_background);
            return null;
        }
        EditText millisecsInput = timeInputView.findViewById(R.id.millisecondsInput);
        try {
            millisecs = Integer.parseInt(millisecsInput.getText().toString());
            if(millisecs>999) throw new Exception();
        } catch (Exception e) {
            millisecsInput.setBackgroundResource(R.drawable.error_background);
            return null;
        }
        //input is valid at this point
        return LocalTime.of(hours, mins, secs, Utils.nanoFromMili(millisecs));
    }

    /**
     * Checks if the given (valid) timings are good for the subtitle component. Also displays a dialog
     * with additional info.
     *
     * Not good if:
     * - End time if before start time.
     * - Start time is before previous component end time.
     * - End time is after next component start time.
     */
    private boolean incorrectTimings(LocalTime startTime, LocalTime endTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.invalid_timings);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        if(startTime.isAfter(endTime)) {
            String message = getString(R.string.end_before_start_error, endTime.format(Formatters.GENERAL_FORMATTER),
                    startTime.format(Formatters.GENERAL_FORMATTER));
            builder.setMessage(message);
            builder.create().show();
            return true;
        } else if(previousComponentEnd!=null && startTime.isBefore(previousComponentEnd)) {
            String additionalInfo = getString(R.string.start_before_prev_error, startTime.format(Formatters.GENERAL_FORMATTER),
                    previousComponentEnd.format(Formatters.GENERAL_FORMATTER));
            builder.setView(createWarningView(additionalInfo));
            builder.create().show();
            return true;
        } else if(nextComponentStart!=null && endTime.isAfter(nextComponentStart)) {
            String additionalInfo = getString(R.string.end_after_next_error, endTime.format(Formatters.GENERAL_FORMATTER),
                    nextComponentStart.format(Formatters.GENERAL_FORMATTER));
            builder.setView(createWarningView(additionalInfo));
            builder.setCancelable(false);
            builder.create().show();
            return true;
        }
        return false; //good, no need to show dialog
    }

    private View createWarningView(String additionalInfo) {
        View warningView = View.inflate(getApplicationContext(), R.layout.overlap_error, null);
        TextView infoView = warningView.findViewById(R.id.additionalInfoText);
        infoView.setText(additionalInfo);
        return warningView;
    }

    /**
     * @return A simple listener that resets the background of the edit text when the text changes.
     */
    public static TextWatcher createTextListener(EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editText.setBackgroundResource(R.drawable.edit_text_background);
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        };
    }
}
