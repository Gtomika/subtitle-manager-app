package com.gaspar.subtitlemanager;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author Muhammad Nabeel Arif (Stackoverflow.com)
 */
public class Utils {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return An int value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(float dp, Context context) {
        float f = dp * ((float) context.getResources().getDisplayMetrics()
                .densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Float.valueOf(f).intValue();
    }

    /*
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return An int value to represent dp equivalent to px value
     */
/*
    public static int convertPixelsToDp(float px, Context context) {
        float f = px / ((float) context.getResources().getDisplayMetrics()
                .densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Float.valueOf(f).intValue();
    }
*/
    /**
     * Converts amounts of milliseconds to nanoseconds.
     */
    public static int nanoFromMili(int miliSecs) {
        return 1000000 * miliSecs;
    }
}
