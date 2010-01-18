package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;

/**
 * <a href="http://hmmer.janelia.org/">HMMER</a> raw match.
 *
 * @author  Antony Quinn
 * @author  Manjula Primma
 * @version $Id$
 */
@Entity
abstract class HmmerRawMatch extends RawMatch  {

    // TODO: evalue and score can be calculated -- do we need to store? Needs testing
    @Column(name="EVALUE",nullable = false, updatable = false)
    private double evalue;
    
    @Column(name="SCORE",nullable = false, updatable = false)
    private double score;

    @Column(name="HMM_START")
    private int hmmStart;

    @Column(name="HMM_END")
    private int hmmEnd;

    @Column(name="HMM_BOUNDS", length = 2)
    private String hmmBounds;

    @Column(name="SEQ_SCORE")
    private double locationScore;

    protected HmmerRawMatch() { }
    
    protected HmmerRawMatch(String sequenceIdentifier, String model,
                            String signatureLibraryName, String signatureLibraryRelease,
                            int locationStart, int locationEnd,
                            double evalue, double score,
                            int hmmStart, int hmmEnd, String hmmBounds,
                            double locationScore, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, generator);
        this.evalue = Math.log10(evalue);
        this.score = score;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.hmmBounds = hmmBounds;
        this.locationScore = locationScore;
    }

    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }

    public int getHmmStart() {
        return hmmStart;
    }

    private void setHmmStart(int hmmStart) {
        this.hmmStart = hmmStart;
    }

    public int getHmmEnd() {
        return hmmEnd;
    }

    private void setHmmEnd(int hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    private void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public double getLocationScore() {
        return locationScore;
    }

    private void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

}