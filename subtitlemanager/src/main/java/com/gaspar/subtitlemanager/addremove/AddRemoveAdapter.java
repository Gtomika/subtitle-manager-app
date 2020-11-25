package com.gaspar.subtitlemanager.addremove;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponentView;
import com.gaspar.subtitlemanager.subtitle.SubtitleModifierActivity;

import org.threeten.bp.LocalTime;
import java.util.ArrayList;

/**
 * An adapter that creates SubtitleComponentView + AddNewComponentView pairs from a
 * subtitle component list.
 */
public class AddRemoveAdapter extends ArrayAdapter<SubtitleComponent> {

    private SubtitleModifierActivity activity;
    private Context context;

    AddRemoveAdapter(SubtitleModifierActivity activity, ArrayList<SubtitleComponent> components) {
        super(((AppCompatActivity)activity).getApplicationContext(), 0, components);
        this.activity = activity;
        this.context = ((AppCompatActivity)activity).getApplicationContext(); //implementors will
        //always be AppCompatActivities
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup viewGroup) {
        SubtitleComponent component = getItem(position);
        convertView = createAddRemoveView(component, activity);
        ImageButton addNewComponentButton = convertView.findViewById(R.id.addNewComponentButton);
        addNewComponentButton.setOnClickListener(view -> {
            LocalTime prevEndTime = null, nextStartTime = null;
            try {
                prevEndTime = activity.getSubtitle().getComponents().get(position).getEndTime();
            } catch (Exception ignored) {}  //most likely index problems
            try {
                nextStartTime = activity.getSubtitle().getComponents().get(position+1).getStartTime();
            } catch (Exception ignored) {} //most likely index problems
            ((AddRemoveActivity)activity).
                    startActivityForComponent(position+1, prevEndTime, nextStartTime); //launch a component creator activity
        });
        return convertView;
    }

    /**
     * Creates a view that holds a subtitle component view and an 'add new component view' under it.
     */
    private View createAddRemoveView(SubtitleComponent component, SubtitleModifierActivity activity) {
        LinearLayout addRemoveView = new LinearLayout(context);
        addRemoveView.setOrientation(LinearLayout.VERTICAL);
        addRemoveView.addView(new SubtitleComponentView(component, context,
                activity.createComponentListener(component))); //add subtitle component view
        LayoutInflater.from(context).inflate(R.layout.add_new_component, addRemoveView); //add new component view
        return addRemoveView;
    }
}
