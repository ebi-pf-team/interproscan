package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class Gene3dHmmer3RawMatch extends Hmmer3RawMatch {

    // Sequence alignment in CIGAR format
    private String cigarAlignment;

    protected Gene3dHmmer3RawMatch() { }

    public Gene3dHmmer3RawMatch(String sequenceIdentifier, String model,
                                String signatureLibraryName, String signatureLibraryRelease,
                                long locationStart, long locationEnd,
                                double evalue, double score,
                                long hmmStart, long hmmEnd, String hmmBounds,
                                double locationScore,
                                int envelopeStart, int envelopeEnd,
                                double expectedAccuracy, double fullSequenceBias,
                                double domainCeValue, double domainIeValue, double domainBias,
                                String cigarAlignment, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias, generator);
        this.cigarAlignment = cigarAlignment;
    }

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    private void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }    

}