package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Raw  match for PIRSR.
 */
@Entity
@Table(name = PIRSRHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "PIRSR_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PIRSR_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PIRSR_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PIRSR_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PIRSR_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PIRSRHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "PIRSR_HMMER3_RAW_MATCH";

    private String scope;

    private  String seqAlignment;
    private String hmmAlignment;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSeqAlignment() {
        return seqAlignment;
    }

    public void setSeqAlignment(String seqAlignment) {
        this.seqAlignment = seqAlignment;
    }

    public String getHmmAlignment() {
        return hmmAlignment;
    }

    public void setHmmAlignment(String hmmAlignment) {
        this.hmmAlignment = hmmAlignment;
    }

    protected PIRSRHmmer3RawMatch() {
    }

    public PIRSRHmmer3RawMatch(String sequenceIdentifier, String model,
                              SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                              int locationStart, int locationEnd,
                              double evalue, double score,
                              int hmmStart, int hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias, String scope,
                               String seqAlignment, String hmmAlignment) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias);
        this.scope = scope;
        this.seqAlignment = seqAlignment;
        this.hmmAlignment = hmmAlignment;
    }

    public PIRSRHmmer3RawMatch getNewRawMatch(String modelAc){
        PIRSRHmmer3RawMatch promotedRawMatch = new PIRSRHmmer3RawMatch(this.getSequenceIdentifier(), modelAc,
                this.getSignatureLibrary(), this.getSignatureLibraryRelease(),
                this.getLocationStart(), this.getLocationEnd(),
                this.getEvalue(), this.getScore(),
                this.getHmmStart(), this.getHmmEnd(), this.getHmmBounds(),
                this.getLocationScore(),
                this.getEnvelopeStart(), this.getEnvelopeEnd(),
                this.getExpectedAccuracy(), this.getFullSequenceBias(),
                this.getDomainCeValue(), this.getDomainIeValue(), this.getDomainBias(), this.getScope(),
                this.getSeqAlignment(), this.getHmmAlignment());
        //Utilities.verboseLog(1100, "Promoted match for " + this.getModelId() + " with new model: " + modelAc + " ::::- " + promotedRawMatch);
        return promotedRawMatch;
    }
}
