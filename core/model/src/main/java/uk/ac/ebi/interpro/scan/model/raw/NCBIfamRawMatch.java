package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * NCBIfam raw match.
 */
@Entity
@Table(name = NCBIfamRawMatch.TABLE_NAME, indexes = {
        @Index(name = "NCBIFAM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "NCBIFAM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "NCBIFAM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "NCBIFAM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "NCBIFAM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class NCBIfamRawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "NCBIFAM_RAW_MATCH";

    protected NCBIfamRawMatch() {
    }

    public NCBIfamRawMatch(String sequenceIdentifier, String model,
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
}
