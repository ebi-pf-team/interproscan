package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @author  Manjula Primma
 * @version $Id$
 */
@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class HmmerRawMatch extends RawMatch  {

    // TODO: evalue and score can be calculated -- do we need to store? Needs testing
    //@Column(name="EVALUE",nullable = false, updatable = false)
    private double evalue;
    
    //@Column(name="SCORE",nullable = false, updatable = false)
    private double score;

    //@Column(name="HMM_START")
    private long hmmStart;

    //@Column(name="HMM_END")
    private long hmmEnd;

    //@Column(name="HMM_BOUNDS")
    private String hmmBounds;

    //@Column(name="LOCATION_EVALUE")
    private double locationEvalue;

    //@Column(name="SEQ_SCORE")
    private double locationScore;

    protected HmmerRawMatch() { }
    
    protected HmmerRawMatch(String sequenceIdentifier, String model,
                            String signatureLibraryName, String signatureLibraryRelease,
                            long locationStart, long locationEnd,
                            double evalue, double score,
                            long hmmStart, long hmmEnd, String hmmBounds,
                            double locationEvalue, double locationScore, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, generator);
        this.evalue = evalue;
        this.score = score;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.hmmBounds = hmmBounds;
        this.locationEvalue = locationEvalue;
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

    public long getHmmStart() {
        return hmmStart;
    }

    private void setHmmStart(long hmmStart) {
        this.hmmStart = hmmStart;
    }

    public long getHmmEnd() {
        return hmmEnd;
    }

    private void setHmmEnd(long hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    private void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public double getLocationEvalue() {
        return locationEvalue;
    }

    private void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = locationEvalue;
    }

    public double getLocationScore() {
        return locationScore;
    }

    private void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

}