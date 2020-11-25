package com.gaspar.subtitlemanager.subtitle;

import java.io.File;

/**
 * Indicates that the subtitle file is invalid.
 *
 * The only irregularities that this application tolerates are:
 *
 * - Invalid timestamps, for example: 00:03:11 instead of 00:03:11:000.
 * These timestamps will not be adjusted and won't be editable.
 *
 * - Component without text, for example:
 * 32
 * 00:03:11:000 --> 00:03:13:000
 * These components will be ignored, and removed from the file when saved.
 *
 * All other problems, such as a component that has invalid or no timings line at all, or one that
 * is unfinished because the file ends will throw this exception.
 */
public class InvalidSubtitleException extends Exception {

    public InvalidSubtitleException(File f) {
        super("Invalid subtitle file: " + f.getName());
    }

}
