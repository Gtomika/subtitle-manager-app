package com.gaspar.subtitlemanager.conversion;

import com.gaspar.subtitlemanager.subtitle.InvalidSubtitleException;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;

import java.io.File;
import org.threeten.bp.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The converter class for SRT subtitle files. This class parses a file with srt extension into a
 * more abstract {@link com.gaspar.subtitlemanager.subtitle.Subtitle} class.
 *
 * This should be used on a background thread.
 */
public class SRTConverter implements Converter {

    private List<String> lines;

    private File srtFile;

    /**
     * @param srtFile This is guaranteed to be and actual srt file at this point.
     */
    SRTConverter(File srtFile) {
        this.srtFile = srtFile;
        try {
            lines = Converter.parseFile(srtFile); //read lines
        } catch (Exception e) {
            throw new IllegalArgumentException("The given file is invalid!");
        }
    }

    @Override
    public Subtitle convert() throws InvalidSubtitleException {
        ArrayList<SubtitleComponent> components = new ArrayList<>();
        for(int i=0; i<lines.size(); i++) {
            if(startOfSubtitleComponent(lines.get(i))) { //start parsing a subtitle component
                String timingsLine = lines.get(i+1);
                timingsLine = timingsLine.replaceAll("\\s+",""); //remove whitespace
                String[] timings = timingsLine.split("-->");
                if(timings.length != 2) throw new InvalidSubtitleException(srtFile);
                LocalTime startTime = null;
                LocalTime endTime = null;
                try {
                    startTime = LocalTime.parse(timings[0], Formatters.SRT_FORMATTER); //start time
                    endTime = LocalTime.parse(timings[1], Formatters.SRT_FORMATTER); //end time
                } catch(Exception ignored) { }  //invalid timings format, leave them as null

                int j = i+2; //text starts at this line
                String text = "";
                while(!lines.get(j).equals("")) { //parse text until we get to the empty line, end of component
                    text = text.concat(lines.get(j)).concat(System.getProperty("line.separator"));
                    j++;
                }
                if(text.length() == 0) continue; //no text component... this component will be ignored.
                text = text.substring(0, text.length()-1); //last linebreak not needed

                SubtitleComponent component = SubtitleComponent.builder() //build component
                        .withText(text)
                        .withStartTime(startTime)
                        .withEndTime(endTime)
                        .withOriginalTimingsLine(lines.get(i+1))
                        .build();
                components.add(component);
                i = j; //move ahead of the component
            }
        }
        return new Subtitle(components);
    }

    /**
     * In case of srt, components start with a positive integer number.
     */
    @Override
    public boolean startOfSubtitleComponent(String line) {
        try {
            int lineSequenceNumber = Integer.parseInt(line.trim());
            return lineSequenceNumber > 0;
        } catch(NumberFormatException exc) {
            return false; //failed to parse, cant be an integer
        }
    }

    /**
     * Handles the other way conversion, from a general subtitle object to valid SRT lines.
     */
    public List<String> toLines(Subtitle subtitle) {
        List<String> lines = new ArrayList<>();
        int i=1; //sequence counter
        for(SubtitleComponent component: subtitle.getComponents()) {
            lines.add(String.valueOf(i++)); //add srt sequence counter line

            String timingsLine;
            if(component.getStartTime()!=null && component.getEndTime()!=null) { //valid timings.
                timingsLine = component.getStartTime().format(Formatters.SRT_FORMATTER) + " ";
                timingsLine = timingsLine.concat("-->");
                timingsLine = timingsLine.concat(component.getEndTime().format(Formatters.SRT_FORMATTER) + " ");
            } else {
                timingsLine = component.getInvalidTimingText(); //rewrite original, invalid line
            }
            lines.add(timingsLine);

            String[] textLines = component.getText().split(System.getProperty("line.separator")); //may have multiple lines
            lines.addAll(Arrays.asList(textLines));
            lines.add(System.getProperty("line.separator")); //add empty line after component
        }
        return lines;
    }
}
