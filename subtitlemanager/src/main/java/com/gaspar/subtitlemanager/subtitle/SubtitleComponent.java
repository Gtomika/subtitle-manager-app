package com.gaspar.subtitlemanager.subtitle;

import androidx.annotation.Nullable;

import org.threeten.bp.LocalTime;

import java.io.Serializable;

/**
 * Represents a part of a subtitle, which contains a displayable text and a timings
 * when to start and when to stop displaying.
 */
public class SubtitleComponent implements Serializable {

    /**
     * The text to be displayed.
     */
    private String text;

    /**
     * Start and end times for the text. Null of the timing was invalid in the subtitle file.
     */
    @Nullable
    private LocalTime startTime, endTime;

    /**
     * If the timings where invalid then the original timing line is saved, and later re written to file.
     */
    @Nullable
    private String invalidTimingText;

    /**
     * @return A component builder object.
     */
    public static SubtitleComponentBuilder builder() {
        return new SubtitleComponentBuilder();
    }

    /**
     * Builder class.
     */
    public static class SubtitleComponentBuilder {

        private String text;
        @Nullable
        private LocalTime startTime, endTime;
        private String originalTimingsLine;

        private SubtitleComponentBuilder() {}

        public SubtitleComponentBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public SubtitleComponentBuilder withStartTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public SubtitleComponentBuilder withEndTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public SubtitleComponentBuilder withOriginalTimingsLine(String timingsLine) {
            this.originalTimingsLine = timingsLine;
            return this;
        }

        public SubtitleComponent build() {
            if(text==null) throw new IllegalStateException("Uninitialized components!");
            if(startTime!=null && endTime!=null) { //valid timings
                return new SubtitleComponent(text, startTime, endTime);
            } else { //invalid timings, keep original timings line
                return new SubtitleComponent(text, originalTimingsLine);
            }
        }
    }

    private SubtitleComponent(String text, @Nullable LocalTime startTime, @Nullable LocalTime endTime) {
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
        this.invalidTimingText = null;
    }

    private SubtitleComponent(String text, @Nullable String originalTimingsLine) {
        this.text = text;
        this.startTime = null;
        this.endTime = null;
        this.invalidTimingText = originalTimingsLine;
    }

    public String getText() {
        return text;
    }

    @Nullable
    public LocalTime getStartTime() {
        return startTime;
    }

    @Nullable
    public LocalTime getEndTime() {
        return endTime;
    }

    @Nullable
    public String getInvalidTimingText() {
        return invalidTimingText;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setStartTime(@Nullable LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(@Nullable LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubtitleComponent component = (SubtitleComponent) o;
        if(getStartTime()==null || getEndTime()==null) { //this is invalid
            return component.getInvalidTimingText()==null && getText().equals(component.getText());
        }
        if(component.getStartTime()==null || component.getEndTime()==null) { //other is invalid
            return getInvalidTimingText()==null && getText().equals(component.getText());
        }
        assert endTime != null;
        assert startTime != null;
        return text.equals(component.text) && startTime.equals(component.startTime) &&
                endTime.equals(component.endTime);
    }
}
