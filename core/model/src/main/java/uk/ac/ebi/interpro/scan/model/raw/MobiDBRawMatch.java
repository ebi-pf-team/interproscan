package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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
@Table(name = MobiDBRawMatch.TABLE_NAME, indexes = {
        @Index(name = "MOBIDB_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "MOBIDB_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "MOBIDB_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "MOBIDB_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "MOBIDB_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class MobiDBRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "MOBIDB_RAW_MATCH";

    /*
     * Example mobi-db lite binary output :
     *
     * UPI000058F405	21	40
        UPI000058F421	720	751
        UPI000058F421	847	881
        UPI000058F422	720	751
        UPI000058F422	845	910
        UPI000058F423	720	790
        UPI000058F423	882	917
     */

    String description;

    protected MobiDBRawMatch() {}

    public MobiDBRawMatch(String sequenceIdentifier, String model,
                          SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                          int locationStart, int locationEnd, String description) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MobiDBRawMatch))
            return false;
        final MobiDBRawMatch m = (MobiDBRawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(this.description, m.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 51)
                .appendSuper(super.hashCode())
                .append(this.getDescription())
                .toHashCode();
    }
}
