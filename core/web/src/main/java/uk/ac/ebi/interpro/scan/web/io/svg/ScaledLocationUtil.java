package uk.ac.ebi.interpro.scan.web.io.svg;

/**
 * Utility to scale location starts and ends.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ScaledLocationUtil {


    public static int getScaledLocationStart(float scaleFactor, int locStart) {
        if (locStart == 1) {
            return 0;
        }
        return Math.round(((float) locStart) * scaleFactor);
    }

    public static int getScaledLocationLength(float scaleFactor, int locStart, int locEnd, int proteinLength) {
        //If blob goes over the whole length
        if (locEnd == proteinLength && locStart == 1) {
            return Math.round(((float) proteinLength) * scaleFactor);
        }
        return Math.round(((float) locEnd - locStart) * scaleFactor);
    }
}
