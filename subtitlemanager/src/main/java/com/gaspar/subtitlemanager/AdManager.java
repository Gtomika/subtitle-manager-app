package com.gaspar.subtitlemanager;

import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Handles ads that can be displayed in the application
 */
public abstract class AdManager {

    /**
     * Loads an ad on the given ad view.
     */
    public static void displayAd(View hopefullyAdView) {
        AdView adView = null;
        try {
            adView = (AdView)hopefullyAdView;
        } catch (ClassCastException ignored) { }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
