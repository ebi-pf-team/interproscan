package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * <a href="http://SFLD.edu/">SFLD</a> raw match.
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
@Table(name = SFLDHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "SFLD_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "SFLD_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "SFLD_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "SFLD_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "SFLD_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class SFLDHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "SFLD_HMMER3_RAW_MATCH";

    protected SFLDHmmer3RawMatch() {
    }

    public SFLDHmmer3RawMatch(String sequenceIdentifier, String model,
                              SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                              int locationStart, int locationEnd,
                              double evalue, double score,
                              int hmmStart, int hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias);
    }

    public SFLDHmmer3RawMatch getNewRawMatch(String modelAc){
        SFLDHmmer3RawMatch promotedRawMatch = new SFLDHmmer3RawMatch(this.getSequenceIdentifier(), modelAc,
                this.getSignatureLibrary(), this.getSignatureLibraryRelease(),
                this.getLocationStart(), this.getLocationEnd(),
                this.getEvalue(), this.getScore(),
                this.getHmmStart(), this.getHmmEnd(), this.getHmmBounds(),
                this.getLocationScore(),
                this.getEnvelopeStart(), this.getEnvelopeEnd(),
                this.getExpectedAccuracy(), this.getFullSequenceBias(),
                this.getDomainCeValue(), this.getDomainIeValue(), this.getDomainBias());
        //Utilities.verboseLog(1100, "Promoted match for " + this.getModelId() + " with new model: " + modelAc + " ::::- " + promotedRawMatch);
        return promotedRawMatch;
    }
}
