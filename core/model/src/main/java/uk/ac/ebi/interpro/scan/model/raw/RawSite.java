package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.KeyGen;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.*;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents "raw site": the output from command line applications such as rpsblast or ...
 * before post-processing.
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class RawSite implements Serializable {

    private static final Pattern SIMPLE_INTEGER_PATTERN = Pattern.compile("^\\d+$");

    // These column names are referenced to define database indexes in sub-classes.
    // In upper case to ensure they work with Oracle.
    public static final String COL_NAME_SEQUENCE_IDENTIFIER = "SEQUENCE_ID";
    public static final String COL_NAME_MODEL_ID = "MODEL_ID";
    public static final String COL_NAME_TITLE = "TITLE";
    public static final String COL_NAME_NUMERIC_SEQUENCE_ID = "NUMERIC_SEQUENCE_ID";
    public static final String COL_NAME_SIGNATURE_LIBRARY = "SIGNATURE_LIBRARY";
    public static final String COL_NAME_SIGNATURE_LIBRARY_RELEASE = "SIG_LIB_RELEASE";


    // TODO: Design for "abstract" (Bloch)
    // TODO: Don't need any foreign keys -- just index fields we will search on

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RAW_SITE_IDGEN")
    @TableGenerator(name = "RAW_SITE_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "site", initialValue = 0, allocationSize = 100)
    private Long id;

    @Column(name = COL_NAME_SEQUENCE_IDENTIFIER)
    // Column explicitly named so subclass indexes can reference column.
    private String sequenceIdentifier;      // eg. MD5

    @Column(name = COL_NAME_NUMERIC_SEQUENCE_ID)    // Column explicitly named so subclass indexes can reference column.
    private Long numericSequenceId;

    @Column(name = COL_NAME_MODEL_ID)               // Column explicitly named so subclass indexes can reference column.
    private String modelId;                   // eg. cd00001

    @Enumerated(EnumType.STRING)
    @Column(name = COL_NAME_SIGNATURE_LIBRARY)
    // Column explicitly named so subclass indexes can reference column.
    private SignatureLibrary signatureLibrary;    // eg. CDD

    @Column(name = COL_NAME_SIGNATURE_LIBRARY_RELEASE)
    // Column explicitly named so subclass indexes can reference column.
    private String signatureLibraryRelease; // eg. 3.14

    @Column(name = COL_NAME_TITLE)               // site name
    private String title;

    @Column(name = "residues", nullable = false, length = 2000)
    private String residues;

    @Column
    private int firstStart;

    @Column
    private int lastEnd;

    protected RawSite() {
    }

    protected RawSite(String sequenceIdentifier, String modelId, String title, String residues,
                      SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        this.setSequenceIdentifier(sequenceIdentifier);
        this.setModelId(modelId);
        this.title = title;
        this.residues = residues;
        this.setSignatureLibrary(signatureLibrary);
        this.setSignatureLibraryRelease(signatureLibraryRelease);

        // Calculate first/last residue postion from residue co-ordinates
        String residueCoordinateList [] = residues.split(",");
        Integer firstStart = null;
        Integer lastEnd = null;
        for (String residueAnnot: residueCoordinateList) {
            //String residue = residueAnnot.substring(0, 1);
            int siteLocation = Integer.parseInt(residueAnnot.substring(1));
            if (firstStart == null || siteLocation < firstStart) {
                firstStart = siteLocation;
            }
            if (lastEnd == null || siteLocation > lastEnd) {
                lastEnd = siteLocation;
            }
        }
        if (firstStart == null) {
            throw new IllegalStateException("First/last residue co-ordinates could not be found from " + residues);
        }

        this.setFirstStart(firstStart);
        this.setLastEnd(lastEnd);
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

    public String getTitle() {
        return title;
    }

    public String getResidues() {
        return residues;
    }

    public int getFirstStart() {
        return firstStart;
    }

    private void setFirstStart(int firstStart) {
        this.firstStart = firstStart;
    }

    public int getLastEnd() {
        return lastEnd;
    }

    private void setLastEnd(int lastEnd) {
        this.lastEnd = lastEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawSite))
            return false;
        final RawSite m = (RawSite) o;
        return new EqualsBuilder()
                .append(sequenceIdentifier, m.sequenceIdentifier)
                .append(signatureLibrary, m.signatureLibrary)
                .append(signatureLibraryRelease, m.signatureLibraryRelease)
                .append(modelId, m.modelId)
                .append(title, m.title)
                .append(firstStart, m.firstStart)
                .append(lastEnd, m.lastEnd)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 51)
                .append(sequenceIdentifier)
                .append(signatureLibrary)
                .append(signatureLibraryRelease)
                .append(modelId)
                .append(title)
                .append(firstStart)
                .append(lastEnd)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
