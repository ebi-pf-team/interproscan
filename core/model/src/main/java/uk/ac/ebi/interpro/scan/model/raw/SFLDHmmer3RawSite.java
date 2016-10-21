package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <a href="http://SFLD.edu/">SFLD</a> raw site.
 */
@Entity
@Table(name = SFLDHmmer3RawSite.TABLE_NAME, indexes = {
        @Index(name = "SFLD_RW_S_SEQ_IDX", columnList = RawSite.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "SFLD_RW_S_NUM_SEQ_IDX", columnList = RawSite.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "SFLD_RW_S_MODEL_IDX", columnList = RawSite.COL_NAME_MODEL_ID),
        @Index(name = "SFLD_RW_S_SIGLIB_IDX", columnList = RawSite.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "SFLD_RW_S_SIGLIB_REL_IDX", columnList = RawSite.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class SFLDHmmer3RawSite extends Hmmer3RawSite implements Comparable<SFLDHmmer3RawSite> {

    public static final String TABLE_NAME = "SFLD_HMMER3_RAW_SITE";

    protected SFLDHmmer3RawSite() {
    }

    public SFLDHmmer3RawSite(String sequenceIdentifier,
                             String title,
                             String residues,
                             String model,
                             String signatureLibraryRelease) {
        super(sequenceIdentifier, model, title, residues, SignatureLibrary.SFLD, signatureLibraryRelease);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than , equal to, or greater than that object.
     * <p/>
     * Maybe the required ordering for CDD post processing is:
     * <p/>
     * evalue ASC, BitScore DESC
     *
     * @param that being the CDDRawSite to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(SFLDHmmer3RawSite that) {
        if (this == that) return 0;
        if (this.getFirstStart() < that.getFirstStart()) return -1;     // First, sort by state ASC
        if (this.getFirstStart() > that.getFirstStart()) return 1;
        if (this.getLastEnd() > that.getLastEnd()) return -1;                     // then by score ASC
        if (this.getLastEnd() < that.getLastEnd()) return 1;
        if (this.hashCode() > that.hashCode())
            return -1;                     // then by hashcode to be consistent with equals.
        if (this.hashCode() < that.hashCode()) return 1;
        return 0;
    }

}
