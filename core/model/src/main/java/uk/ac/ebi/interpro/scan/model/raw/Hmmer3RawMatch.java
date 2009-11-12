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

    protected Hmmer3RawMatch() { }

    protected Hmmer3RawMatch(String sequenceIdentifier, String model,
                             String signatureLibraryName, String signatureLibraryRelease,
                             long locationStart, long locationEnd,
                             double evalue, double score,
                             long hmmStart, long hmmEnd, String hmmBounds,
                             double locationEvalue, double locationScore,
                             int envelopeStart, int envelopeEnd,
                             double expectedAccuracy, double fullSequenceBias,
                             double domainCeValue, double domainIeValue, double domainBias,
                             String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationEvalue, locationScore, generator);
        this.envelopeStart = envelopeStart;
        this.envelopeEnd = envelopeEnd;
        this.expectedAccuracy = expectedAccuracy;
        this.fullSequenceBias = fullSequenceBias;
        this.domainCeValue = domainCeValue;
        this.domainIeValue = domainIeValue;
        this.domainBias = domainBias;
    }

    public int getEnvelopeStart() {
        return envelopeStart;
    }

    private void setEnvelopeStart(int envelopeStart) {
        this.envelopeStart = envelopeStart;
    }

    public int getEnvelopeEnd() {
        return envelopeEnd;
    }

    private void setEnvelopeEnd(int envelopeEnd) {
        this.envelopeEnd = envelopeEnd;
    }

    public double getExpectedAccuracy() {
        return expectedAccuracy;
    }

    private void setExpectedAccuracy(double expectedAccuracy) {
        this.expectedAccuracy = expectedAccuracy;
    }

    public double getFullSequenceBias() {
        return fullSequenceBias;
    }

    private void setFullSequenceBias(double fullSequenceBias) {
        this.fullSequenceBias = fullSequenceBias;
    }

    public double getDomainCeValue() {
        return domainCeValue;
    }

    private void setDomainCeValue(double domainCeValue) {
        this.domainCeValue = domainCeValue;
    }

    public double getDomainIeValue() {
        return domainIeValue;
    }

    private void setDomainIeValue(double domainIeValue) {
        this.domainIeValue = domainIeValue;
    }

    public double getDomainBias() {
        return domainBias;
    }

    private void setDomainBias(double domainBias) {
        this.domainBias = domainBias;
    }

// TODO: Add hashCode(), equals() ...etc

}