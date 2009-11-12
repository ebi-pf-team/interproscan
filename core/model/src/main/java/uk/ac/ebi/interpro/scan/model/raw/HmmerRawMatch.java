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
    
    public HmmerRawMatch() { }

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