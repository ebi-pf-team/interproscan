package uk.ac.ebi.interpro.scan.model.raw;

import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;

/**
 * <a href="http://TIGR.sanger.ac.uk/">TigrFAM</a> raw match.
 *
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@javax.persistence.Table(name = TigrFamHmmer3RawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = TigrFamHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "TIGR3_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "TIGR3_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "TIGR3_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "TIGR3_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "TIGR3_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class TigrFamHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "TIGRFAM_HMMER3_RAW_MATCH";

    protected TigrFamHmmer3RawMatch() {
    }

    public TigrFamHmmer3RawMatch(String sequenceIdentifier, String model,
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
