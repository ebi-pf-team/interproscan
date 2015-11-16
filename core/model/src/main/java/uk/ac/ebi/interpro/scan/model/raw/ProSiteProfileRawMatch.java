package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/prosite/">PROSITE</a> Profile raw match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name = ProSiteProfileRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PRSITE_PROF_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PRSITE_PROF_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PRSITE_PROF_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PRSITE_PROF_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PRSITE_PROF_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class ProSiteProfileRawMatch extends ProfileScanRawMatch {

    public static final String TABLE_NAME = "PROSITE_PROF_RAW_MATCH";

    protected ProSiteProfileRawMatch() {
    }

    public ProSiteProfileRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryRelease,
                                  int locationStart, int locationEnd, String cigarAlignment, double score, Level level) {
        super(sequenceIdentifier, model, SignatureLibrary.PROSITE_PROFILES, signatureLibraryRelease,
                locationStart, locationEnd, cigarAlignment, score, level);
    }

}
