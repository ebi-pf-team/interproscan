package uk.ac.ebi.interpro.scan.model.raw;

import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * <a href="http://www.pantherdb.org/">PANTHER</a> raw match. All attributes of a Panther
 * raw match are stored, even if they are not required at the moment but maybe in the future.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@Entity
@javax.persistence.Table(name = PantherRawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = PantherRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PANTHER_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "PANTHER_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "PANTHER_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "PANTHER_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "PANTHER_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class PantherRawMatch extends RawMatch {

    public static final String TABLE_NAME = "PANTHER_RAW_MATCH";

    @Column
    private double evalue;

    /* Name of the Panther family/subfamily for instance SYNAPTOTAGMIN */
    @Column
    private String familyName;

    @Column
    private double score;

    protected PantherRawMatch() {
    }

    /**
     * @param model      Model Id (Panther family Id) for instance PTHR10024 OR PTHR10024:SF2.
     * @param score      Calculated by the Panther Perl algorithm/binary.
     * @param familyName The name of the Panther family/subfamily (for instance SYNAPTOTAGMIN).
     */
    public PantherRawMatch(String sequenceIdentifier, String model,
                           String signatureLibraryRelease,
                           int locationStart, int locationEnd,
                           double evalue, double score, String familyName) {
        super(sequenceIdentifier, model, SignatureLibrary.PANTHER, signatureLibraryRelease, locationStart, locationEnd);
        setEvalue(evalue);
        this.score = score;
        this.familyName = familyName;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
