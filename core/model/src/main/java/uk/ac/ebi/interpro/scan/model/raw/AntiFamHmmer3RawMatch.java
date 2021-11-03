package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * <a href="https://www.ebi.ac.uk/research/bateman/software/antifam-tool-identify-spurious-proteins">AntiFam</a> raw match.
 *
 * @author Matthias Blum
 * @version $Id$
 */
@Entity
@Table(name = AntiFamHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "ANTIFAM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "ANTIFAM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "ANTIFAM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "ANTIFAM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "ANTIFAM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class AntiFamHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "ANTIFAM_HMMER3_RAW_MATCH";

    protected AntiFamHmmer3RawMatch() {
    }

    public AntiFamHmmer3RawMatch(String sequenceIdentifier, String model,
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
