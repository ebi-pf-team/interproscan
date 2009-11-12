package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public abstract class Hmmer3RawMatch extends HmmerRawMatch {

    private int envelopeStart;
    private int envelopeEnd;
    
    private double expectedAccuracy;

    private double fullSequenceBias;

    private double domainCeValue;
    private double domainIeValue;
    private double domainBias;

    public int getEnvelopeStart() {
        return envelopeStart;
    }

    public void setEnvelopeStart(int envelopeStart) {
        this.envelopeStart = envelopeStart;
    }

    public int getEnvelopeEnd() {
        return envelopeEnd;
    }

    public void setEnvelopeEnd(int envelopeEnd) {
        this.envelopeEnd = envelopeEnd;
    }

    public double getExpectedAccuracy() {
        return expectedAccuracy;
    }

    public void setExpectedAccuracy(double expectedAccuracy) {
        this.expectedAccuracy = expectedAccuracy;
    }

    public double getFullSequenceBias() {
        return fullSequenceBias;
    }

    public void setFullSequenceBias(double fullSequenceBias) {
        this.fullSequenceBias = fullSequenceBias;
    }

    public double getDomainCeValue() {
        return domainCeValue;
    }

    public void setDomainCeValue(double domainCeValue) {
        this.domainCeValue = domainCeValue;
    }

    public double getDomainIeValue() {
        return domainIeValue;
    }

    public void setDomainIeValue(double domainIeValue) {
        this.domainIeValue = domainIeValue;
    }

    public double getDomainBias() {
        return domainBias;
    }

    public void setDomainBias(double domainBias) {
        this.domainBias = domainBias;
    }

// TODO: Add hashCode(), equals() ...etc

}