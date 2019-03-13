package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * MOBIDB raw match.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = CoilsRawMatch.TABLE_NAME, indexes = {
        @Index(name = "Coils_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "Coils_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "Coils_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "Coils_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "Coils_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class CoilsRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "COILS_RAW_MATCH";

    /*
     * Example coils binary output :
     *
     * UPI000058F405	21	40
        UPI000058F421	720	751
     */



    protected CoilsRawMatch() {}

    public CoilsRawMatch(String sequenceIdentifier, String model,
                         SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                         int locationStart, int locationEnd) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
    }

}
