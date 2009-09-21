package uk.ac.ebi.interpro.scan.model.raw;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class ProfileScanRawMatch extends RawMatch {

    private double score; // location.score

    public ProfileScanRawMatch() { }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
}
