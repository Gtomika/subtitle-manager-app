package com.gaspar.subtitlemanager.adjusting;

import com.gaspar.subtitlemanager.Utils;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;

import org.threeten.bp.LocalTime;

/**
 * This class adjusts the timings of abstract {@link com.gaspar.subtitlemanager.subtitle.Subtitle}
 * objects.
 */
class Adjuster {

    private Subtitle subtitle;

    Adjuster(Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Adjusts the subtitle's timings.
     *
     * @param operation Delay or speed up operation.
     * @param millisecs The amount of milliseconds to adjust.
     */
    void adjust(AdjusterActivity.Operation operation, int millisecs) throws IllegalArgumentException {
        if(millisecs < 0) throw new IllegalArgumentException("Must be a positive amount!");
        if(millisecs == 0) return; //no need to change anything

        for(SubtitleComponent component: subtitle.getComponents()) {
            if(component.getStartTime()==null || component.getEndTime()==null) { //corrupted component
                continue; //cant do modification on this, just go to the next
            }
            LocalTime newStartTime, newEndTime;
            if(operation == AdjusterActivity.Operation.SPEED_UP) {
                newStartTime = component.getStartTime().minusNanos(Utils.nanoFromMili(millisecs));
                newEndTime = component.getEndTime().minusNanos(Utils.nanoFromMili(millisecs));

                //if the new time is after original, then it "under flew", set to 00:00:00,000
                if(newStartTime.isAfter(component.getStartTime())) {
                    newStartTime = LocalTime.of(0,0);
                    long nanoDifference = component.getEndTime().toNanoOfDay()
                            - component.getStartTime().toNanoOfDay();
                    //display time will remain the same, but from the start of the video
                    newEndTime = LocalTime.ofNanoOfDay(nanoDifference);
                }
            } else { //delay
                newStartTime = component.getStartTime().plusNanos(Utils.nanoFromMili(millisecs));
                newEndTime = component.getEndTime().plusNanos(Utils.nanoFromMili(millisecs));

                //if the new time is before original, then it "overflew", set to 23:59:99,999
                if(newEndTime.isBefore(component.getEndTime())) {
                    newEndTime = LocalTime.MAX;
                    long nanoDifference = component.getEndTime().toNanoOfDay()
                            - component.getStartTime().toNanoOfDay();
                    //display time will remain the same, but at the "end of the video"
                    //ONLY if the video is a day long, otherwise it wont be shown
                    newStartTime = LocalTime.MAX.minusNanos(nanoDifference);
                }
            }
            component.setStartTime(newStartTime); //save new timings
            component.setEndTime(newEndTime);
        }
    }
}
