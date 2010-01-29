package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://pfam.sanger.ac.uk/">Pfam</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="pfam_hmmer3_raw_match")
public class PfamHmmer3RawMatch extends Hmmer3RawMatch {

    protected PfamHmmer3RawMatch() { }    

    public PfamHmmer3RawMatch(String sequenceIdentifier, String model,
                              String signatureLibraryName, String signatureLibraryRelease,
                              int locationStart, int locationEnd,
                              double evalue, double score,
                              int hmmStart, int hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd,
              evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias);
    }
}
