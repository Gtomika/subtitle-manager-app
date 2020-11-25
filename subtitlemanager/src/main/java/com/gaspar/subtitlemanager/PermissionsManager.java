package com.gaspar.subtitlemanager;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Manages app permissions. This object always belongs to an activity
 */
public class PermissionsManager {

    /**
     * Permission request codes. These are used in the apps onPermissionRequestResult method.
     */
    public static final int READ_REQUEST_CODE = 0, WRITE_REQUEST_CODE = 1;

    /**
     * The activity of this object.
     */
    private Activity activity;

    public PermissionsManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Requests a permission for the activity. Should only be called if the permission isn't
     * already granted.
     */
     public void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission}, requestCode);
    }

    /**
     * Checks if the activity has a permission.
     */
    public boolean doesntHavePermission(String permission) {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED;
    }
}
