package com.gaspar.subtitlemanager.conversion;

import org.threeten.bp.format.DateTimeFormatter;

/**
 * Contains local time formatters.
 */
public class Formatters {

    /**
     * A formatter that formats a time object to the standard display format, HH:MM:SS,NS
     */
    public static final DateTimeFormatter GENERAL_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    /**
     * A time formatter tuned to the standard timestamps of an SRT file, 00:03:32,500 for example.
     */
    static final DateTimeFormatter SRT_FORMATTER
            = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");

    static final DateTimeFormatter VTT_FORMATTER
            = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
}
