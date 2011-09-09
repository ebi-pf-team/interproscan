package uk.ac.ebi.interpro.scan.io.tmhmm;

/**
 * Simple wrapper class. Wraps TMHMM predictions and the associated max score.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PredictionMaxScoreWrapper {
    private TMHMMPrediction prediction;
    private float maxScore;

    private PredictionMaxScoreWrapper() {
    }

    public PredictionMaxScoreWrapper(TMHMMPrediction prediction, float maxScore) {
        this.prediction = prediction;
        this.maxScore = maxScore;
    }

    public TMHMMPrediction getPrediction() {
        return prediction;
    }

    public float getMaxScore() {
        return maxScore;
    }
}
