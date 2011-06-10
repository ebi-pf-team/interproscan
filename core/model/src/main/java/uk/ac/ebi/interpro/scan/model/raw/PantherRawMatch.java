package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.pantherdb.org/">PANTHER</a> raw match.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@Entity
@Table(name = PantherRawMatch.TABLE_NAME)
public class PantherRawMatch extends RawMatch {

    public static final String TABLE_NAME = "PANTHER_RAW_MATCH";

    @Column
    private double evalue;

    @Column
    private String familyName;

    @Column
    private double score;

    protected PantherRawMatch() {
    }

    public PantherRawMatch(String sequenceIdentifier, String model,
                           String signatureLibraryRelease,
                           int locationStart, int locationEnd,
                           double evalue, double score, String familyName) {
        super(sequenceIdentifier, model, SignatureLibrary.PANTHER, signatureLibraryRelease, locationStart, locationEnd);
        this.evalue = evalue;
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