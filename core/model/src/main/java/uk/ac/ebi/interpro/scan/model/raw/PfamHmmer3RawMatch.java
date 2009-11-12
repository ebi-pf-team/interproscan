package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class PfamHmmer3RawMatch extends Hmmer3RawMatch {

    // TODO: Consider Builder to make more readable

    protected PfamHmmer3RawMatch() { }    

    public PfamHmmer3RawMatch(String sequenceIdentifier, String model,
                              String signatureLibraryName, String signatureLibraryRelease,
                              long locationStart, long locationEnd,
                              double evalue, double score,
                              long hmmStart, long hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias,
                              String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias, generator);
    }
}
