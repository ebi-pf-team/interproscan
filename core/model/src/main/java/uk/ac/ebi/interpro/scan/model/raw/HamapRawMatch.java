package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/sprot/hamap/">HAMAP</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = HamapRawMatch.TABLE_NAME, indexes = {
        @Index(name = "HAMAP_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "HAMAP_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "HAMAP_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "HAMAP_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "HAMAP_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class HamapRawMatch extends ProfileScanRawMatch {

    public static final String TABLE_NAME = "HAMAP_RAW_MATCH";

    protected HamapRawMatch() {
    }

    public HamapRawMatch(String sequenceIdentifier, String model,
                         String signatureLibraryRelease,
                         int locationStart, int locationEnd, String cigarAlignment, double score, Level level) {
        super(sequenceIdentifier, model, SignatureLibrary.HAMAP, signatureLibraryRelease,
                locationStart, locationEnd, cigarAlignment, score, level);
    }
}
