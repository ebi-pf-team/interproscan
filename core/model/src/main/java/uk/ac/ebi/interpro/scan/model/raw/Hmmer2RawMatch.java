package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public abstract class Hmmer2RawMatch extends HmmerRawMatch {

    private double locationEvalue;    

    protected Hmmer2RawMatch() { }    

    protected Hmmer2RawMatch(String sequenceIdentifier, String model,
                             String signatureLibraryName, String signatureLibraryRelease,
                             int locationStart, int locationEnd,
                             double evalue, double score,
                             int hmmStart, int hmmEnd, String hmmBounds,
                             double locationEvalue, double locationScore, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, generator);
        this.locationEvalue = Math.log10(locationEvalue);
    }

    public double getLocationEvalue() {
        return locationEvalue;
    }

    private void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = locationEvalue;
    }
    
}