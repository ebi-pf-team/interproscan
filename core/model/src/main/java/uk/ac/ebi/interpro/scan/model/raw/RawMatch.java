package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
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

    // TODO: Design for "abstract" (Bloch)
    // TODO: Don't need any foreign keys -- just index fields we will search on

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RAW_MATCH_IDGEN")
    @TableGenerator(name = "RAW_MATCH_IDGEN", table = "KEYGEN", pkColumnValue = "match", initialValue = 0, allocationSize = 100)
    private Long id;

    @Index(name = "rawmatch_seq_id_idx")
    @Column(name = "sequence_id")
    private String sequenceIdentifier;      // eg. MD5

    @Index(name = "rawmatch_num_seq_id_idx")
    @Column(name = "numeric_seq_id")
    private Long numericSequenceId;

    @Index(name = "rawmatch_model_idx")
    @Column(name = "model_id")
    private String modelId;                   // eg. PF00001

    @Index(name = "rawmatch_lib_idx")
    @Enumerated(javax.persistence.EnumType.STRING)
    private SignatureLibrary signatureLibrary;    // eg. PFAM

    @Index(name = "rawmatch_release_idx")
    @Column(name = "sig_lib_release")
    private String signatureLibraryRelease; // eg. 23.0

    @Column(name = "location_start")
    private int locationStart;

    @Column(name = "location_end")
    private int locationEnd;

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

    private void setModelId(String modelId) {
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
