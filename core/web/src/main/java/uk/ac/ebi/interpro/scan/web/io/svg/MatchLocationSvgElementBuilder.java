package uk.ac.ebi.interpro.scan.web.io.svg;

import uk.ac.ebi.interpro.scan.web.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchLocationSvgElementBuilder {

    private List<SimpleLocation> simpleLocations;

    public MatchLocationSvgElementBuilder(SimpleSignature simpleSignature) {
        if (simpleSignature != null) {
            this.simpleLocations = simpleSignature.getLocations();
        }
    }

    public StringBuilder build(final int proteinLength, final Map<String, Integer> entryColourMap,
                               final String entryType, final String entryAccession, final String scale) {
        final float rectangleWidth = 930;
        final float scaleFactor = rectangleWidth / (float) proteinLength;
        final int lineHeight = 17;
        final StringBuilder result = new StringBuilder();
        String[] scaleMarkers = scale.split(",");
        if (simpleLocations != null && simpleLocations.size() > 0) {
//            result.append("<g>");
            result.append("<rect width=\"" + rectangleWidth + "px\" height=\"" + 17 + "px\" style=\"fill:#EFEFEF\"/>");
            ScaleMarkerUtil.appendScaleMarkers(result, scaleMarkers, scaleFactor, lineHeight);
            for (SimpleLocation simpleLocation : simpleLocations) {
                int locStart = simpleLocation.getStart();
                int locEnd = simpleLocation.getEnd();
                int scaledLocationStart = Math.round(((float) locStart-1) * scaleFactor);
                int scaledRectangleWidth = Math.round(((float) locEnd - locStart) * scaleFactor);

                result.append("<rect");
                result.append(" ");
                appendColourClass(result, entryType, entryColourMap, entryAccession);
                result.append("x=\"" + scaledLocationStart + "px\" y=\"" + 5 + "px\" width=\"" + scaledRectangleWidth + "px\" height=\"" + 7 + "px\"");
                result.append(" ");
                result.append("rx=\"3.984848\" ry=\"5.6705141\"");
                result.append(" ");
                result.append("style=\"stroke:black;stroke-width:0.3\">");
                result.append("<title>" + locStart + " - " + locEnd + "</title>");
                result.append("</rect>");
            }
//            result.append("</g>");
        }
        return result;
    }

    private void appendColourClass(final StringBuilder result, final String entryType,
                                   final Map<String, Integer> entryColourMap,
                                   final String entryAccession) {
        if (entryType != null) {
            result.append("class=\"");
            if (entryType.equalsIgnoreCase(EntryType.DOMAIN.toString())
                    || entryType.equalsIgnoreCase(EntryType.REPEAT.toString())) {
                final Integer colourCode = entryColourMap.get(entryAccession);
                if (colourCode != null) {
                    result.append("c" + colourCode);
                }
            } else if (entryType.equalsIgnoreCase(EntryType.UNKNOWN.toString())) {
                result.append("uni");

            } else {
                result.append(entryType);
            }
            result.append("\" ");
        }
    }

//    public static void main(String[] args) {
//        float scaleFactor = (float) 930 / (float) 639;
//        System.out.println(scaleFactor);
//        double scaleFactor2 = (double) 930 / (double) 639;
//        System.out.println(scaleFactor2);
//
//        float scaledLocationStartFloat = ((float) 25) * scaleFactor;
//        System.out.println(scaledLocationStartFloat);
//
//        int scaledLocationStartInt = (int) (((float) 25) * scaleFactor);
//        System.out.println(scaledLocationStartInt);
//
//    }
}