package com.gaspar.subtitlemanager.subtitle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.Utils;
import com.gaspar.subtitlemanager.conversion.Formatters;

/**
 * A view that shows a subtitle component. It's a vertical linear layout
 * with 2 text views, one for the subtitle text and one for the timings.
 *
 * Can also take an on click listener.
 */
public class SubtitleComponentView extends LinearLayout {

    /**
     * The component that this view displays.
     */
    private SubtitleComponent component;

    private Context context;

    public SubtitleComponentView(SubtitleComponent component, Context context, OnClickListener listener) {
        super(context);
        this.component = component;
        this.context = context;

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setLayoutParams(createOuterParams());
        int padding = Utils.convertDpToPixel(5, context);
        setPadding(padding, padding, padding, padding);

        TextView textDisplayer = new TextView(context);
        textDisplayer.setMaxLines(2);
        textDisplayer.setText(component.getText());
        addView(textDisplayer);

        addView(createTimingsView());
        if(listener!=null) setOnClickListener(listener); //register the on click listener
    }

    /**
     * @return The text view that displays the timings.
     */
    private TextView createTimingsView() {
        TextView timingsView = new TextView(context);
        String timingsText;
        if(component.getStartTime()!=null && component.getEndTime()!=null) {
            timingsText = component.getStartTime().format(Formatters.GENERAL_FORMATTER); //concat end and start
            timingsText = timingsText.concat(" - ").concat(component.getEndTime().format(Formatters.GENERAL_FORMATTER));
        } else { //components timing is invalid
            timingsText = component.getInvalidTimingText();
            timingsView.setBackgroundResource(R.drawable.warning_background); //display that something is wrong
        }
        timingsView.setText(timingsText);
        timingsView.setLines(1);
        return timingsView;
    }

    /**
     * @return layout params for the main linear layout.
     */
    private ListView.LayoutParams createOuterParams() {
        return new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public SubtitleComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubtitleComponentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
