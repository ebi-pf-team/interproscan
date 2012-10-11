package uk.ac.ebi.interpro.scan.web.io.svg;

/**
 * Utility to render the scale markers for SVG.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ScaleMarkerUtil {

    public static void appendScaleMarkers(final StringBuilder result, final String[] scaleMarkers,
                                          final float scaleFactor, final int lineHeight) {
        ScaleMarkerUtil.appendScaleMarkers(result, scaleMarkers, scaleFactor, lineHeight, false);
    }

    public static void appendScaleMarkers(final StringBuilder result, final String[] scaleMarkers,
                                          final float scaleFactor, final int lineHeight, boolean isLastLineCondensedView) {
        for (String scaleMarker : scaleMarkers) {
            int scaleMarkerInt = Integer.parseInt(scaleMarker);
            int marker;
            //If it is the first scale marker
            marker = Math.round(((float) scaleMarkerInt) * scaleFactor);
//            if (scaleMarkerInt == 0) {
//                marker = Math.round(((float) scaleMarkerInt) * scaleFactor);
//            } else {
//                marker = Math.round(((float) scaleMarkerInt - 1) * scaleFactor);
//            }
            result.append("<line x1=\"" + marker + "px\" y1=\"0px\" x2=\"" + marker + "px\" y2=\"" + (isLastLineCondensedView ? (lineHeight + 5) : lineHeight) + "px\" style=\"stroke:#B8B8B8;stroke-width:1;stroke-dasharray:3.0\"/>");
            if (isLastLineCondensedView) {
                int leftShift = getLeftShift(scaleMarker);
                result.append("<text x=\"" + (marker - leftShift) + "px\" y=\"" + (lineHeight + 15) + "px\" font-size=\"12\"");
                result.append(" ");
                result.append("style=\"fill:#B8B8B8;font-family:Verdana, Helvetica, sans-serif;font-weight:normal\">");
                if (scaleMarkerInt == 0) {
                    scaleMarkerInt++;
                }
                result.append(scaleMarkerInt);
                result.append("</text>");
            }
        }
    }

    private static int getLeftShift(String scaleMarker) {
        switch (scaleMarker.length()) {
            case 2:
                return 8;
            case 3:
                return 10;
            case 4:
                return 14;
            default:
                return 0;
        }
    }
}