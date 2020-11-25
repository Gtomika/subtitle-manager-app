package com.gaspar.subtitlemanager.subtitle;

import java.util.ArrayList;

/**
 * Represents an entire subtitle file with texts and timings. This class has no concept of
 * subtitle file extensions, the conversion classes handle that.
 */
public class Subtitle {

    /**
     * Components of the subtitle file.
     */
    private ArrayList<SubtitleComponent> components;

    public Subtitle(ArrayList<SubtitleComponent> components) {
        this.components = components;
    }

    public ArrayList<SubtitleComponent> getComponents() {
        return components;
    }
}
