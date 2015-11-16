package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <a href="http://prodom.prabi.fr/">ProDom</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = ProDomRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PRODOM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PRODOM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PRODOM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PRODOM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PRODOM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class ProDomRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "PRODOM_RAW_MATCH";

    // Example ProDom output file:
    // UPI00004BBFB1      1    198 //  pd_PD400414;sp_U689_HUMAN_Q6UX39;       1    206 // S=426    E=1e-41  //  (3)  PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED 	 Length = 206

    @Column
    private String spId; // e.g. sp_U689_HUMAN_Q6UX39

    @Column
    private int sigMatchLocationStart;

    @Column
    private int sigMatchLocationEnd;

    @Column
    private int score;

    @Column
    private double evalue;

    @Column
    private int numDomainsInFamily;

    @Column
    private String methodName;

    protected ProDomRawMatch() {
    }

    public ProDomRawMatch(String sequenceIdentifier,
                          String model,
                          String signatureLibraryRelease,
                          int locationStart,
                          int locationEnd,
                          String spId,
                          int sigMatchLocationStart,
                          int sigMatchLocationEnd,
                          int score,
                          double evalue,
                          int numDomainsInFamily,
                          String methodName) {
        super(sequenceIdentifier, model, SignatureLibrary.PRODOM, signatureLibraryRelease, locationStart, locationEnd);
        this.spId = spId;
        this.sigMatchLocationStart = sigMatchLocationStart;
        this.sigMatchLocationEnd = sigMatchLocationEnd;
        this.score = score;
        this.evalue = evalue;
        this.numDomainsInFamily = numDomainsInFamily;
        this.methodName = methodName;
    }

    public String getSpId() {
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public int getSigMatchLocationStart() {
        return sigMatchLocationStart;
    }

    public void setSigMatchLocationStart(int sigMatchLocationStart) {
        this.sigMatchLocationStart = sigMatchLocationStart;
    }

    public int getSigMatchLocationEnd() {
        return sigMatchLocationEnd;
    }

    public void setSigMatchLocationEnd(int sigMatchLocationEnd) {
        this.sigMatchLocationEnd = sigMatchLocationEnd;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public int getNumDomainsInFamily() {
        return numDomainsInFamily;
    }

    public void setNumDomainsInFamily(int numDomainsInFamily) {
        this.numDomainsInFamily = numDomainsInFamily;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProDomRawMatch))
            return false;
        final ProDomRawMatch m = (ProDomRawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(spId, m.spId)
                .append(sigMatchLocationStart, m.sigMatchLocationStart)
                .append(sigMatchLocationEnd, m.sigMatchLocationEnd)
                .append(score, m.score)
                .append(evalue, m.evalue)
                .append(numDomainsInFamily, m.numDomainsInFamily)
                .append(methodName, m.methodName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 81)
                .appendSuper(super.hashCode())
                .append(spId)
                .append(sigMatchLocationStart)
                .append(sigMatchLocationEnd)
                .append(score)
                .append(evalue)
                .append(numDomainsInFamily)
                .append(methodName)
                .toHashCode();
    }

}
