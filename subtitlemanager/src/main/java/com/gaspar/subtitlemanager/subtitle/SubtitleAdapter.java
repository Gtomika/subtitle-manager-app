package com.gaspar.subtitlemanager.subtitle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * An adapter that creates {@link SubtitleComponentView}s for an
 * {@link android.widget.ListView}.
 */
public class SubtitleAdapter extends ArrayAdapter<SubtitleComponent> {

    private SubtitleModifierActivity activity;
    private Context context;

    SubtitleAdapter(SubtitleModifierActivity activity, ArrayList<SubtitleComponent> components) {
        super(((AppCompatActivity)activity).getApplicationContext(), 0, components);
        this.activity = activity;
        this.context = ((AppCompatActivity)activity).getApplicationContext(); //implementors will
                                                                                //always be AppCompatActivities
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup viewGroup) {
        SubtitleComponent component = getItem(position);
        assert component != null;
        convertView = new SubtitleComponentView(component, context, activity.createComponentListener(component));
        return convertView;
    }
}
