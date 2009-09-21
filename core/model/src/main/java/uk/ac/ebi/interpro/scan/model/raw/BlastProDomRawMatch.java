package uk.ac.ebi.interpro.scan.model.raw;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class BlastProDomRawMatch extends RawMatch {

    private double score;   // location.score
    
    public BlastProDomRawMatch() { }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
