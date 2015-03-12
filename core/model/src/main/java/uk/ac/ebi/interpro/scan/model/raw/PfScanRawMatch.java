package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Abstract model for all Prosite type matches (including HAMAP, Prosite Profiles, Prosite Patterns).
 * <p/>
 * Subclassed directly by Prosite Patterns.  Subclassed by second abstract class ProfileScanRawMatch
 * and hence by HamapRawMatch and PrositeProfileRawMatch.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
public abstract class PfScanRawMatch extends RawMatch {

    @Column(nullable = false, length = 4000)
    private String cigarAlignment;

    protected PfScanRawMatch() {
    }

    protected PfScanRawMatch(String sequenceIdentifier, String model,
                             SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                             int locationStart, int locationEnd,
                             String cigarAlignment) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        setCigarAlignment(cigarAlignment);
    }

    private void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PfScanRawMatch)) return false;
        if (!super.equals(o)) return false;

        PfScanRawMatch that = (PfScanRawMatch) o;

        if (!cigarAlignment.equals(that.cigarAlignment)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + cigarAlignment.hashCode();
        return result;
    }
}
