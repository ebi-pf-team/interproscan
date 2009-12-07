package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="gene3d_hmmer3_raw_match")
public class Gene3dHmmer3RawMatch extends Hmmer3RawMatch {

    // Sequence alignment in CIGAR format
    private String cigarAlignment;

    protected Gene3dHmmer3RawMatch() { }

    public Gene3dHmmer3RawMatch(String sequenceIdentifier, String model,
                                String signatureLibraryName, String signatureLibraryRelease,
                                int locationStart, int locationEnd,
                                double evalue, double score,
                                int hmmStart, int hmmEnd, String hmmBounds,
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