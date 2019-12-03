package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * TMHMM raw match.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = TMHMMRawMatch.TABLE_NAME, indexes = {
        @Index(name = "TMHMM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "TMHMM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "TMHMM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "TMHMM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "TMHMM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class TMHMMRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "TMHMM_RAW_MATCH";

    /*
     * Example tmhmm binary output :
     *
     * UPI000058F405	21	40
        UPI000058F421	720	751
        UPI000058F421	847	881
        UPI000058F422	720	751
        UPI000058F422	845	910
        UPI000058F423	720	790
        UPI000058F423	882	917
     */


    protected TMHMMRawMatch() {}

    public TMHMMRawMatch(String sequenceIdentifier, String model,
                         SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                         int locationStart, int locationEnd) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
    }

}
