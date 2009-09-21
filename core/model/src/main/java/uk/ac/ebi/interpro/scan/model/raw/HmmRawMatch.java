package uk.ac.ebi.interpro.scan.model.raw;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class HmmRawMatch extends RawMatch {

    // TODO: evalue and score can be calculated -- do we need to store? Needs testing
    private double evalue;
    private double score;
    private long hmmStart;
    private long hmmEnd;
    private String hmmBounds;    
    private double locationEvalue;
    private double locationScore;
    private String alignment;   // CIGAR format

    public HmmRawMatch() { }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getHmmStart() {
        return hmmStart;
    }

    public void setHmmStart(long hmmStart) {
        this.hmmStart = hmmStart;
    }

    public long getHmmEnd() {
        return hmmEnd;
    }

    public void setHmmEnd(long hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public double getLocationEvalue() {
        return locationEvalue;
    }

    public void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = locationEvalue;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

}
