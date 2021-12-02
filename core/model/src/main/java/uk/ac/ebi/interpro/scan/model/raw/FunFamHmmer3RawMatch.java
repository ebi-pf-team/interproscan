package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.*;

@Entity
@Table(name = FunFamHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "FUNFAM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "FUNFAM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "FUNFAM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "FUNFAM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "FUNFAM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class FunFamHmmer3RawMatch extends Hmmer3RawMatch {

    @Transient
    public static final String TABLE_NAME = "FUNFAM_HMMER3_RAW_MATCH";

    protected FunFamHmmer3RawMatch() {
    }

    public FunFamHmmer3RawMatch(String sequenceIdentifier, String model,
                                String signatureLibraryRelease,
                                int locationStart, int locationEnd,
                                double evalue, double score,
                                int hmmStart, int hmmEnd, String hmmBounds,
                                double locationScore,
                                int envelopeStart, int envelopeEnd,
                                double expectedAccuracy, double fullSequenceBias,
                                double domainCeValue, double domainIeValue, double domainBias) {
        super(sequenceIdentifier, model, SignatureLibrary.FUNFAM, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy,
                fullSequenceBias, domainCeValue, domainIeValue, domainBias);
    }
}
