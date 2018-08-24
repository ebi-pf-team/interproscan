package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.DCStatus;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.KeyGen;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.*;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents "raw matches": the output from command line applications such as HMMER or pfscan
 * before post-processing.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class RawMatch implements Serializable {

    private static final Pattern SIMPLE_INTEGER_PATTERN = Pattern.compile("^\\d+$");

    // These column names are referenced to define database indexes in sub-classes.
    // In upper case to ensure they work with Oracle.
    public static final String COL_NAME_SEQUENCE_IDENTIFIER = "SEQUENCE_ID";
    public static final String COL_NAME_MODEL_ID = "MODEL_ID";
    public static final String COL_NAME_NUMERIC_SEQUENCE_ID = "NUMERIC_SEQUENCE_ID";
    public static final String COL_NAME_SIGNATURE_LIBRARY = "SIGNATURE_LIBRARY";
    public static final String COL_NAME_SIGNATURE_LIBRARY_RELEASE = "SIG_LIB_RELEASE";


    // TODO: Design for "abstract" (Bloch)
    // TODO: Don't need any foreign keys -- just index fields we will search on

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RAW_MATCH_IDGEN")
    @TableGenerator(name = "RAW_MATCH_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "match", initialValue = 0, allocationSize = 100)
    private Long id;

    @Column(name = COL_NAME_SEQUENCE_IDENTIFIER)
    // Column explicitly named so subclass indexes can reference column.
    private String sequenceIdentifier;      // eg. MD5

    @Column(name = COL_NAME_NUMERIC_SEQUENCE_ID)    // Column explicitly named so subclass indexes can reference column.
    private Long numericSequenceId;

    @Column(name = COL_NAME_MODEL_ID)               // Column explicitly named so subclass indexes can reference column.
    private String modelId;                   // eg. PF00001

    @Enumerated(javax.persistence.EnumType.STRING)
    @Column(name = COL_NAME_SIGNATURE_LIBRARY)
    // Column explicitly named so subclass indexes can reference column.
    private SignatureLibrary signatureLibrary;    // eg. PFAM

    @Column(name = COL_NAME_SIGNATURE_LIBRARY_RELEASE)
    // Column explicitly named so subclass indexes can reference column.
    private String signatureLibraryRelease; // eg. 23.0

    @Column
    private int locationStart;

    @Column
    private int locationEnd;

    @Column
    private String locFragmentDCStatus;

    protected RawMatch() {
    }

    protected RawMatch(String sequenceIdentifier, String modelId,
                       SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                       int locationStart, int locationEnd) {
        this.setSequenceIdentifier(sequenceIdentifier);
        this.setModelId(modelId);
        this.setSignatureLibrary(signatureLibrary);
        this.setSignatureLibraryRelease(signatureLibraryRelease);
        this.setLocationStart(locationStart);
        this.setLocationEnd(locationEnd);
        this.locFragmentDCStatus = DCStatus.CONTINUOUS.getSymbol();
    }

    protected RawMatch(String sequenceIdentifier, String modelId,
                       SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                       int locationStart, int locationEnd, String locFragmentDCStatus) {
        this.setSequenceIdentifier(sequenceIdentifier);
        this.setModelId(modelId);
        this.setSignatureLibrary(signatureLibrary);
        this.setSignatureLibraryRelease(signatureLibraryRelease);
        this.setLocationStart(locationStart);
        this.setLocationEnd(locationEnd);
        this.locFragmentDCStatus = locFragmentDCStatus;
    }

    public Long getId() {
        return id;
    }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    private void setSequenceIdentifier(String sequenceIdentifier) {
        if (sequenceIdentifier == null) {
            this.sequenceIdentifier = null;
        } else {
            this.sequenceIdentifier = sequenceIdentifier.trim();
            if (SIMPLE_INTEGER_PATTERN.matcher(this.sequenceIdentifier).matches()) {
                this.numericSequenceId = Long.parseLong(this.sequenceIdentifier);
            }
        }
    }

    public Long getNumericSequenceId() {
        return numericSequenceId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    private void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    private void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public int getLocationStart() {
        return locationStart;
    }

    private void setLocationStart(int locationStart) {
        this.locationStart = locationStart;
    }

    public int getLocationEnd() {
        return locationEnd;
    }

    private void setLocationEnd(int locationEnd) {
        this.locationEnd = locationEnd;
    }

    public String getLocFragmentDCStatus() {
        return locFragmentDCStatus;
    }

    public void setLocFragmentDCStatus(String locFragmentDCStatus) {
        this.locFragmentDCStatus = locFragmentDCStatus;
    }

    /**
     * Determines if two domains overlap.
     *
     * @param other domain match other.
     * @return true if the two domain matches overlap.
     */
    public boolean overlapsWith(RawMatch other) {
        return !
                ((this.getLocationStart() > other.getLocationEnd()) ||
                        (other.getLocationStart() > this.getLocationEnd()));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawMatch))
            return false;
        final RawMatch m = (RawMatch) o;
        return new EqualsBuilder()
                .append(sequenceIdentifier, m.sequenceIdentifier)
                .append(signatureLibrary, m.signatureLibrary)
                .append(signatureLibraryRelease, m.signatureLibraryRelease)
                .append(modelId, m.modelId)
                .append(locationStart, m.locationStart)
                .append(locationEnd, m.locationEnd)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 51)
                .append(sequenceIdentifier)
                .append(signatureLibrary)
                .append(signatureLibraryRelease)
                .append(modelId)
                .append(locationStart)
                .append(locationEnd)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Listener for raw match to filtered match conversions.
     *
     * @author Antony Quinn
     */
    public interface Listener {

        /**
         * Returns signature instance corresponding to model accession, and signature library name and release version.
         *
         * @param modelAccession          {@see uk.ac.ebi.interpro.scan.model.Model#getAccession()}
         * @param signatureLibrary        {@see uk.ac.ebi.interpro.scan.model.SignatureLibrary}
         * @param signatureLibraryRelease {@see uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease#getVersion()}
         * @return Signature instance corresponding to model accession, and signature library name and release version
         */
        public Signature getSignature(String modelAccession, SignatureLibrary signatureLibrary, String signatureLibraryRelease);

    }

}
