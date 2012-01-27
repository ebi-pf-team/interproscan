package uk.ac.ebi.interpro.scan.web;

/**
 * Common code shared by classes that perform protein related calculation tasks.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinViewHelper {

    /**
     * Generate a string of comma-separated numbers that will be used to mark the scale of the match diagram.
     * @param maxNumScaleMarkers Maximum number of scale markers (should be 2 or more)!
     * @return Comma-separated list of marker positions
     */
    public static String generateScaleMarkers(int proteinLength, int maxNumScaleMarkers) {
        if (maxNumScaleMarkers < 2) {
            // Doesn't make sense! Zero and protein length should always be included whatever happens!
            maxNumScaleMarkers = 2;
        }
        int scale = calcScale(proteinLength, maxNumScaleMarkers);
        StringBuilder sb = new StringBuilder("0"); // The first scale marker (position zero)
        int index = 0;
        int numRemaining = proteinLength;
        while (index <= proteinLength) {
            index += scale;
            numRemaining-= scale;
            sb.append(",");
            if ((numRemaining > 0) && (numRemaining < scale)) {
                // This will be the penultimate marker, unless...
                if (numRemaining < (scale / 2)) {
                    // Not many amino acids remaining, may as well just use the protein length
                    // (E.g. Don't want something like "0,200,400,401" since the 400 and 401 would be too close
                    // together to display both bits of text)!
                    sb.append(proteinLength);
                    break;
                }

            }
            if (index >= proteinLength) {
                // The last scale marker (at protein length position)!
                sb.append(proteinLength);
            }
            else {
                // Append and continue
                sb.append(index);
            }
        }
        return sb.toString();
    }

    /**
     * Calculate scale marker intervals.
     *
     * @param maxNumScaleMarkers Maximum number of scale markers
     * @return Default gap between each scale markers
     */
    private static int calcScale(int proteinLength, int maxNumScaleMarkers) {
        int scale = 1;
        while (true) {
            if (proteinLength / (scale) <= maxNumScaleMarkers) {
                return scale;
            }
            else if (proteinLength / (scale*2) <= maxNumScaleMarkers) {
                return scale*2;
            }
            else if (proteinLength / (scale*5) <= maxNumScaleMarkers) {
                return scale*5;
            }
            scale *= 10;
        }
    }
}
