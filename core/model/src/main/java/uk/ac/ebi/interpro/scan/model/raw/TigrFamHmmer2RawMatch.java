package uk.ac.ebi.interpro.scan.model.raw;

import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;

/**
 * Specific raw match class for Tigrfam.
 * Only required to ensure it gets it's own table.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@javax.persistence.Table(name = TigrFamHmmer2RawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = TigrFamHmmer2RawMatch.TABLE_NAME, indexes = {
        @Index(name = "TIGRFAM2_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "TIGRFAM2_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "TIGRFAM2_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "TIGRFAM2_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "TIGRFAM2_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class TigrFamHmmer2RawMatch extends Hmmer2RawMatch {

    public static final String TABLE_NAME = "TIGRFAM_H2_RAW_MATCH";

    protected TigrFamHmmer2RawMatch() {

    }

    public TigrFamHmmer2RawMatch(String sequenceIdentifier, String model, SignatureLibrary signatureLibrary, String signatureLibraryRelease, int locationStart, int locationEnd, double evalue, double score, int hmmStart, int hmmEnd, String hmmBounds, double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationEvalue, locationScore);
    }
}
