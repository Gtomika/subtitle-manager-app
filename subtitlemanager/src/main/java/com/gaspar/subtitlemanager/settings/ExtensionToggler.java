package com.gaspar.subtitlemanager.settings;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.gaspar.subtitlemanager.R;
import com.gaspar.subtitlemanager.Utils;
import com.gaspar.subtitlemanager.conversion.Extensions;

/**
 * A custom switch. These are added into the settings activity.
 */
public class ExtensionToggler extends Switch implements CompoundButton.OnCheckedChangeListener {

    private SettingsActivity activity;

    private String extensionName;

    public ExtensionToggler(SettingsActivity activity, String extensionName, boolean shouldBeChecked) {
        super(activity);
        this.activity = activity;
        this.extensionName = extensionName;
        setChecked(shouldBeChecked);
        setSwitchPadding(Utils.convertDpToPixel(20, activity));
        setText(extensionName.concat(" files")); //texts
        int p = Utils.convertDpToPixel(10, activity);
        setPadding(p,p,p,p);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //custom thumb
            setShowText(true);
            setTextOff(activity.getString(R.string.disabled));
            setTextOn(activity.getString(R.string.enabled));
            setThumbResource(R.drawable.app_switch_thumb);
        }
        setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putBoolean(extensionName, isChecked()).apply();
        //only if all togglers are registered
        if(activity.extensionTogglers.size() < Extensions.getSupportedExtensionNames().length) return;
        int counter = 0;
        for(ExtensionToggler toggler: activity.extensionTogglers) {
            if(!toggler.isChecked()) counter++;
        }
        TextView warningView = activity.findViewById(R.id.noExtensionWarningView);
        if(counter == activity.extensionTogglers.size()) { //all is unchecked
            warningView.setVisibility(VISIBLE);
        } else {
            warningView.setVisibility(GONE);
        }
    }

    public ExtensionToggler(Context context, AttributeSet set) {
        super(context, set);
    }

    public ExtensionToggler(Context context, AttributeSet set, int defStyleAttr) {
        super(context, set, defStyleAttr);
    }
}
