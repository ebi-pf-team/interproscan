package uk.ac.ebi.interpro.scan.io.tmhmm;

import uk.ac.ebi.interpro.scan.model.TMHMMSignature;

/**
 * Simple wrapper class. Wraps TMHMM predictions and the associated max score.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PredictionMaxScoreWrapper {
    private TMHMMSignature prediction;
    private float maxScore;

    private PredictionMaxScoreWrapper() {
    }

    public PredictionMaxScoreWrapper(TMHMMSignature prediction, float maxScore) {
        this.prediction = prediction;
        this.maxScore = maxScore;
    }

    public TMHMMSignature getPrediction() {
        return prediction;
    }

    public float getMaxScore() {
        return maxScore;
    }
}
