package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = PirsfHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "PIRSF3_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PIRSF3_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PIRSF3_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PIRSF3_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PIRSF3_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PirsfHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "PIRSF_HMMER3_RAW_MATCH";

    @Transient
    private boolean significant;

    @Transient
    private int modelLength;

    @Transient
    private int sequenceLength;

    protected PirsfHmmer3RawMatch() {

    }

    public PirsfHmmer3RawMatch(String sequenceIdentifier, String model,
                               SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                               int locationStart, int locationEnd,
                               double evalue, double score,
                               int hmmStart, int hmmEnd, String hmmBounds,
                               double locationScore,
                               int envelopeStart, int envelopeEnd,
                               double expectedAccuracy, double fullSequenceBias,
                               double domainCeValue, double domainIeValue, double domainBias,
                               boolean significant) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy,
                fullSequenceBias, domainCeValue, domainIeValue, domainBias);
        this.significant = significant;
    }

    public boolean isSignificant() {
        return significant;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    public int getModelLength() {
        return modelLength;
    }

    public void setModelLength(int modelLength) {
        this.modelLength = modelLength;
    }
}