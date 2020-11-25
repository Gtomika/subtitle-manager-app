package com.gaspar.subtitlemanager.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Adapter for the {@link ExtensionToggler} list view
 */
public class ExtensionTogglerAdapter extends BaseAdapter  {

    private ExtensionToggler[] togglers;

    ExtensionTogglerAdapter(List<ExtensionToggler> togglers) {
        this.togglers = togglers.toArray(new ExtensionToggler[] {});
    }

    public int getCount() {
        return togglers.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    /**
     * Creates an {@link ExtensionToggler} for each adapter reference.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ExtensionToggler toggler = togglers[position];
            toggler.setLayoutParams(createParams());
            return toggler;
        } else{
            return convertView;
        }
    }

    private ListView.LayoutParams createParams() {
        return new ListView.LayoutParams(
                ListView.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT){
        };
    }

}
