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
 * @author Gift Nuka
 * @author Matthias Blum
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

    @Column
    private double score;

    @Column(nullable = false)
    private int hmmStart;

    @Column(nullable = false)
    private int hmmEnd;

    @Column(nullable = false)
    private int hmmLength;

    @Column(length = 2)
    private String hmmBounds;

    @Column(nullable = false)
    private int envelopeStart;

    @Column(nullable = false)
    private int envelopeEnd;

    @Column
    private String annotationsNodeId;

    protected PantherRawMatch() {
    }

    public PantherRawMatch(String sequenceIdentifier, String modelId, String signatureLibraryRelease, int locationStart,
                           int locationEnd, double evalue, double score, int hmmStart, int hmmEnd, int hmmLength,
                           String hmmBounds, int envelopeStart, int envelopeEnd, String annotationsNodeId) {
        super(sequenceIdentifier, modelId, SignatureLibrary.PANTHER, signatureLibraryRelease, locationStart, locationEnd);
        this.evalue = evalue;
        this.score = score;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.hmmLength = hmmLength;
        this.hmmBounds = hmmBounds;
        this.envelopeStart = envelopeStart;
        this.envelopeEnd = envelopeEnd;
        this.annotationsNodeId = annotationsNodeId;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
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

    public int getHmmLength() {
        return hmmLength;
    }

    public void setHmmLength(int hmmLength) {
        this.hmmLength = hmmLength;
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

    public String getAnnotationsNodeId() {
        return annotationsNodeId;
    }

    public void setAnnotationsNodeId(String annotationsNodeId) {
        this.annotationsNodeId = annotationsNodeId;
    }
}
