package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

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

    @Column(nullable = false)
    private boolean significant;

    @Column(nullable = false)
    private int sequenceLength;

    protected PirsfHmmer3RawMatch() {

    }

    public PirsfHmmer3RawMatch(int locationEnd, int locationStart, String model, String sequenceIdentifier, double evalue,
                               String hmmBounds, int hmmStart, int hmmEnd, double locationScore, double score,
                               double domainBias, double domainCeValue, double domainIeValue, int envelopeStart,
                               int envelopeEnd, double expectedAccuracy, double fullSequenceBias,
                               SignatureLibrary signatureLibrary, String signatureLibraryRelease, boolean significant) {
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
}