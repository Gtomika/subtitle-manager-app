package com.gaspar.subtitlemanager.conversion;

import com.gaspar.subtitlemanager.subtitle.InvalidSubtitleException;
import com.gaspar.subtitlemanager.subtitle.Subtitle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for converter classes
 */
public interface Converter {

    /**
     * @return the general subtitle file object converted from the given file.
     */
    Subtitle convert() throws InvalidSubtitleException;

    /**
     * Checks if a line is the start of a subtitle component (timing + text).
     */
    boolean startOfSubtitleComponent(String line);

    /**
     * Creates valid lines for the file format of the converter from general
     * subtitle object.
     */
    List<String> toLines(Subtitle subtitle);

    /**
     * Finds a converter object based on the files extension.
     */
    static Converter findConverter(File f) {
        String[] splitResult = f.getName().split("\\.");
        String extension = splitResult[splitResult.length-1];
        if(extension.equals("")) throw new IllegalArgumentException("No extension!"); //no extension

        if(extension.equals(Extensions.Extension.SRT.shortExtensionName())) {
            return new SRTConverter(f);
        } else if(extension.equals(Extensions.Extension.VTT.shortExtensionName())) {
            return new VTTConverter(f);
        } //add more supported extension here
        throw new IllegalArgumentException("Unsupported file!");
    }

    /**
     * Reads the lines of the given file. This method is always called in the converter
     * object's constructor.
     */
    static List<String> parseFile(File f) throws IOException {
        List<String> lines = new ArrayList<>();
        FileInputStream is = new FileInputStream(f);
        try(BufferedReader reader = new BufferedReader(new UnicodeReader(is, null))) {
            String line;
            while ((line = reader.readLine()) != null) {
               lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Writes the given lines to the given file. This should be called on a background thread.
     *
     * @return True only if the writing was successful.
     */
    static boolean saveToFile(File f, List<String> lines) {
        try(FileWriter writer = new FileWriter(f);
            BufferedWriter bWriter = new BufferedWriter(writer)) {
            for(String line: lines) {
                bWriter.write(line);
                bWriter.newLine();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
