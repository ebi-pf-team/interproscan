package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * <a href="http://www.pantherdb.org/">PANTHER</a> raw match. All attributes of a Panther
 * raw match are stored, even if they are not required at the moment but maybe in the future.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@Entity
@Table(name = PantherRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PANTHER_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PANTHER_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PANTHER_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PANTHER_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PANTHER_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
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

    @Column(nullable = false)
    private int hmmStart;

    @Column(nullable = false)
    private int hmmEnd;

    @Column(length = 2)
    private String hmmBounds;

    @Column(nullable = false)
    private int envelopeStart;

    @Column(nullable = false)
    private int envelopeEnd;


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
                           double evalue, double score, String familyName,
                           int hmmStart, int hmmEnd, String hmmBounds, int envelopeStart, int envelopeEnd) {
        super(sequenceIdentifier, model, SignatureLibrary.PANTHER, signatureLibraryRelease, locationStart, locationEnd);
        setEvalue(evalue);
        this.score = score;
        this.familyName = familyName;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.hmmBounds = hmmBounds;
        this.envelopeStart = envelopeStart;
        this.envelopeEnd = envelopeEnd;
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

    public int getHmmStart() {
        return hmmStart;
    }

    public void setHmmStart(int hmmStart) {
        this.hmmStart = hmmStart;
    }

    public int getHmmEnd() {
        return hmmEnd;
    }

    public void setHmmEnd(int hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public int getEnvelopeStart() {
        return envelopeStart;
    }

    public void setEnvelopeStart(int envelopeStart) {
        this.envelopeStart = envelopeStart;
    }

    public int getEnvelopeEnd() {
        return envelopeEnd;
    }

    public void setEnvelopeEnd(int envelopeEnd) {
        this.envelopeEnd = envelopeEnd;
    }
}
