package com.gaspar.subtitlemanager.conversion;

import com.gaspar.subtitlemanager.subtitle.InvalidSubtitleException;
import com.gaspar.subtitlemanager.subtitle.Subtitle;
import com.gaspar.subtitlemanager.subtitle.SubtitleComponent;

import org.threeten.bp.LocalTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The converter class for VTT subtitle files. This class parses a file with vtt extension into a
 * more abstract {@link com.gaspar.subtitlemanager.subtitle.Subtitle} object.
 *
 * This should be used on a background thread.
 */
public class VTTConverter implements Converter {

    /**
     * WebVTT files start with this.
     */
    private static final String VTT_IDENTIFIER = "WEBVTT";

    private List<String> lines;

    private File vttFile;

    VTTConverter(File vttFile) {
        this.vttFile = vttFile;
        try {
            lines = Converter.parseFile(vttFile); //read lines
        } catch (Exception e) {
            throw new IllegalArgumentException("The given file is invalid!");
        }
    }

    @Override
    public Subtitle convert() throws InvalidSubtitleException {
        ArrayList<SubtitleComponent> components = new ArrayList<>();
        if(lines.isEmpty()) return new Subtitle(components); //file can be empty
        if(!lines.get(0).startsWith(VTT_IDENTIFIER)) throw new InvalidSubtitleException(vttFile); //must start with this

        for(int i=1; i<lines.size(); i++) {
            if(startOfSubtitleComponent(lines.get(i))) { //start parsing a subtitle component
                String timingsLine = lines.get(i); //starts with timings line
                timingsLine = clearTimingsLine(timingsLine); //strip unnecessary parts
                String[] timings = timingsLine.split("-->");
                if(timings.length != 2) throw new InvalidSubtitleException(vttFile);
                LocalTime startTime = null;
                LocalTime endTime = null;
                try {
                    startTime = LocalTime.parse(timings[0], Formatters.VTT_FORMATTER); //start time
                    endTime = LocalTime.parse(timings[1], Formatters.VTT_FORMATTER); //end time
                } catch(Exception ignored) { }  //invalid timings format, leave them as null

                int j = i+1; //text starts at this line
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
                        .withOriginalTimingsLine(lines.get(i))
                        .build();
                components.add(component);
                i = j; //move ahead of the component
            }
        }
        return new Subtitle(components);
    }

    /**
     * Strips unsupported parts from the timings line. For example
     * '00:00:05.333 --> 00:00:07.312 align:end' will be reduced to '00:00:05.333-->00:00:07.312'
     */
    private String clearTimingsLine(String timingsLine) {
        timingsLine = timingsLine.replaceAll("\\s+",""); //remove whitespaces
        int i = timingsLine.length()-1;
        while(!Character.isDigit(timingsLine.charAt(i--))) { //remove metadata from the end
            timingsLine = timingsLine.substring(0, timingsLine.length()-1);
        }
        return timingsLine;
    }

    /**
     * .vtt file's components start with the timings line.
     */
    @Override
    public boolean startOfSubtitleComponent(String line) {
        return line.contains("-->");
    }

    @Override
    public List<String> toLines(Subtitle subtitle) {
        List<String> lines = new ArrayList<>();

        lines.add(VTT_IDENTIFIER); //must start with this string
        lines.add(System.getProperty("line.separator"));

        for(SubtitleComponent component: subtitle.getComponents()) {
            String timingsLine; //component begins with timings line
            if(component.getStartTime()!=null && component.getEndTime()!=null) { //valid timings.
                timingsLine = component.getStartTime().format(Formatters.VTT_FORMATTER) + " ";
                timingsLine = timingsLine.concat("-->");
                timingsLine = timingsLine.concat(component.getEndTime().format(Formatters.VTT_FORMATTER) + " ");
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
