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

    @Column(name = "cigar_align", nullable = false)
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
}
