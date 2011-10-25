package uk.ac.ebi.interpro.scan.io.tmhmm;

import uk.ac.ebi.interpro.scan.model.TMHMMSignature;

/**
 * Represents a line parser for the following line example:
 * <p/>
 * M      0.20497   0.77531   0.01971   0.00000
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 * @deprecated This parser still works fine, but was replaced by {@link TMHMMRawResultParser} within TMHMMParseStep. The reason for that is, that the format
 *             of the raw result output changed completely after changing the set of program parameters.
 */
@Deprecated
public class TMHMMPredictionLineParser {

    protected static PredictionMaxScoreWrapper parsePredictionLine(String line) {
        PredictionMaxScoreWrapper result = null;
        String[] splitLine = line.split("\\s+");
        int len = splitLine.length;
        if (len == 5) {
            //Get value for column i
            float scoreInside = Float.parseFloat(splitLine[1].trim());
            //Get value for column O
            float scoreO = Float.parseFloat(splitLine[2].trim());
            //Get value for column o
            float scoreOutside = Float.parseFloat(splitLine[3].trim());
            //Get value for column M
            float scoreMembrane = Float.parseFloat(splitLine[4].trim());
            //Get max score value and prediction
            result = getPredictionValue(scoreInside, scoreO, scoreOutside, scoreMembrane);
        }
        return result;
    }

    /**
     * Determines largest float value of all specified parameter values.
     *
     * @return Prediction value.
     */
    private static PredictionMaxScoreWrapper getPredictionValue(float scoreInside, float scoreO, float scoreOutside, float scoreMembrane) {
        TMHMMSignature result = TMHMMSignature.INSIDE_CELL;
        float maxScore = scoreInside;
        if (scoreO > maxScore) {
            result = TMHMMSignature.OTHER;
            maxScore = scoreO;
        }
        if (scoreOutside > maxScore) {
            result = TMHMMSignature.OUTSIDE_CELL;
            maxScore = scoreOutside;
        }
        if (scoreMembrane > maxScore) {
            result = TMHMMSignature.MEMBRANE;
            maxScore = scoreMembrane;
        }
        return new PredictionMaxScoreWrapper(result, maxScore);
    }
}