package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * Specific raw match class for PIRSF.
 * Only required to ensure it gets it's own table.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = PIRSFHmmer2RawMatch.TABLE_NAME, indexes = {
        @Index(name = "PIRSF2_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PIRSF2_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PIRSF2_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PIRSF2_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PIRSF2_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PIRSFHmmer2RawMatch extends Hmmer2RawMatch {

    public static final String TABLE_NAME = "PIRSF_HMMER2_RAW_MATCH";

    /* Set of subfamily identifiers for this PIRSF match */
    @Transient
    private Set<String> subFamilies = new HashSet<String>();

    protected PIRSFHmmer2RawMatch() {

    }

    public PIRSFHmmer2RawMatch(String sequenceIdentifier, String model, SignatureLibrary signatureLibrary, String signatureLibraryRelease, int locationStart, int locationEnd, double evalue, double score, int hmmStart, int hmmEnd, String hmmBounds, double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationEvalue, locationScore);
    }

    private void addSubFamily(String subFamilyID) {
        subFamilies.add(subFamilyID);
    }

    public void addSubFamilies(Set<String> subFamilies) {
        for (String subFamilyID : subFamilies) {
            addSubFamily(subFamilyID);
        }
    }

    public Set<String> getSubFamilies() {
        return subFamilies;
    }
}