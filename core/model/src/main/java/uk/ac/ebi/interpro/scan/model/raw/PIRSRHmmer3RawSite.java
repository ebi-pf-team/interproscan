package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Raw site class for PIRSR.
 */
@Entity
@Table(name = PIRSRHmmer3RawSite.TABLE_NAME, indexes = {
        @Index(name = "PIRSR_RW_S_SEQ_IDX", columnList = RawSite.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PIRSR_RW_S_NUM_SEQ_IDX", columnList = RawSite.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PIRSR_RW_S_MODEL_IDX", columnList = RawSite.COL_NAME_MODEL_ID),
        @Index(name = "PIRSR_RW_S_SIGLIB_IDX", columnList = RawSite.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PIRSR_RW_S_SIGLIB_REL_IDX", columnList = RawSite.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PIRSRHmmer3RawSite extends Hmmer3RawSite implements Comparable<PIRSRHmmer3RawSite> {

    public static final String TABLE_NAME = "PIRSR_HMMER3_RAW_SITE";

    int ruleAlignStart;

    int ruleAlignEnd;

    String ruleCondition;

    @Column (name = "SITE_LABEL")
    String label;

    @Column (name = "SITE_GROUP")
    int group;

    @Column //(name = "HMMSTART")
    int hmmStart;

    @Column //(name = "HMMEND")
    int hmmEnd;

    protected PIRSRHmmer3RawSite(){

    }

    public PIRSRHmmer3RawSite(String sequenceIdentifier,
                              String title,
                              String residues,
                              String model,
                              String signatureLibraryRelease) {
        super(sequenceIdentifier, model, title, residues, SignatureLibrary.PIRSR, signatureLibraryRelease);
    }

    public PIRSRHmmer3RawSite(String sequenceIdentifier,
                             String title,
                             String residues,
                             String label,
                             int start,
                             int end,
                             int hmmStart,
                             int hmmEnd,
                             int group,
                             int ruleAlignStart,
                             int ruleAlignEnd,
                             String ruleCondition,
                             String model,
                             String signatureLibraryRelease) {
        super(sequenceIdentifier, model, title, residues, start, end, SignatureLibrary.PIRSR, signatureLibraryRelease);
        this.ruleAlignStart = ruleAlignStart;
        this.ruleAlignEnd = ruleAlignEnd;
        this.ruleCondition = ruleCondition;
        this.label = label;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.group = group;
    }

    public int getRuleAlignStart() {
        return ruleAlignStart;
    }

    public void setRuleAlignStart(int ruleAlignStart) {
        this.ruleAlignStart = ruleAlignStart;
    }

    public int getRuleAlignEnd() {
        return ruleAlignEnd;
    }

    public void setRuleAlignEnd(int ruleAlignEnd) {
        this.ruleAlignEnd = ruleAlignEnd;
    }

    public String getRuleCondition() {
        return ruleCondition;
    }

    public void setRuleCondition(String ruleCondition) {
        this.ruleCondition = ruleCondition;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
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


    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than , equal to, or greater than that object.
     * <p/>
     * Maybe the required ordering for CDD post processing is:
     * <p/>
     * evalue ASC, BitScore DESC
     *
     * @param that being the CDDRawSite to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(PIRSRHmmer3RawSite that) {
        if (this == that) return 0;
        if (this.getFirstStart() < that.getFirstStart()) return -1;     // First, sort by state ASC
        if (this.getFirstStart() > that.getFirstStart()) return 1;
        if (this.getLastEnd() > that.getLastEnd()) return -1;                     // then by score ASC
        if (this.getLastEnd() < that.getLastEnd()) return 1;
        if (this.hashCode() > that.hashCode())
            return -1;                     // then by hashcode to be consistent with equals.
        if (this.hashCode() < that.hashCode()) return 1;
        return 0;
    }

//    @Override
//    public String toString() {
//        return "PIRSRHmmer3RawSite{" +
//                "label='" + label + '\'' +
//                ", group=" + group +
//                ", hmmStart=" + hmmStart +
//                ", hmmEnd=" + hmmEnd +
//                '}';
//    }
}
