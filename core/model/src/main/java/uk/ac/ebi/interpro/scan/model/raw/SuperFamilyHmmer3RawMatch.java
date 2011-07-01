package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * SuperFamily raw match
 *
 * @author Matthew Fraser
 * @version $Id$
 */
@Entity
@javax.persistence.Table(name = SuperFamilyHmmer3RawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = SuperFamilyHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "SUPERFAMILY_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "SUPERFAMILY_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "SUPERFAMILY_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "SUPERFAMILY_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "SUPERFAMILY_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class SuperFamilyHmmer3RawMatch extends RawMatch {

    @Transient
    public static final String TABLE_NAME = "SUPERFAMILY_HMMER3_RAW_MATCH";

    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false)
    private int modelMatchStartPos;

    @Column(nullable = false, length = 4000)
    private String aligmentToModel;

    @Column(nullable = false)
    private double familyEvalue;

    @Column(nullable = false)
    private int scopDomainId;

    @Column(nullable = false)
    private int scopFamilyId;

    protected SuperFamilyHmmer3RawMatch() {
    }

    public SuperFamilyHmmer3RawMatch(String sequenceIdentifier, String model,
                                String signatureLibraryRelease,
                                int locationStart, int locationEnd,
                                double evalue, int modelMatchStartPos,
                                String alignmentToModel, double familyEvalue,
                                int scopDomainId, int scopFamilyId) {
        super(sequenceIdentifier, model, SignatureLibrary.SUPERFAMILY, signatureLibraryRelease, locationStart, locationEnd);
        this.evalue = evalue;
        this.modelMatchStartPos = modelMatchStartPos;
        this.aligmentToModel = alignmentToModel;
        this.familyEvalue = familyEvalue;
        this.scopDomainId = scopDomainId;
        this.scopFamilyId = scopFamilyId;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public int getModelMatchStartPos() {
        return modelMatchStartPos;
    }

    public void setModelMatchStartPos(int modelMatchStartPos) {
        this.modelMatchStartPos = modelMatchStartPos;
    }

    public String getAligmentToModel() {
        return aligmentToModel;
    }

    public void setAligmentToModel(String aligmentToModel) {
        this.aligmentToModel = aligmentToModel;
    }

    public double getFamilyEvalue() {
        return familyEvalue;
    }

    public void setFamilyEvalue(double familyEvalue) {
        this.familyEvalue = familyEvalue;
    }

    public int getScopDomainId() {
        return scopDomainId;
    }

    public void setScopDomainId(int scopDomainId) {
        this.scopDomainId = scopDomainId;
    }

    public int getScopFamilyId() {
        return scopFamilyId;
    }

    public void setScopFamilyId(int scopFamilyId) {
        this.scopFamilyId = scopFamilyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SuperFamilyHmmer3RawMatch))
            return false;
        final SuperFamilyHmmer3RawMatch m = (SuperFamilyHmmer3RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getEvalue(), m.getEvalue())
                .append(getModelMatchStartPos(), m.getModelMatchStartPos())
                .append(getAligmentToModel(), m.getAligmentToModel())
                .append(getFamilyEvalue(), m.getFamilyEvalue())
                .append(getScopDomainId(), m.getScopDomainId())
                .append(getScopFamilyId(), m.getScopFamilyId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 61)
                .appendSuper(super.hashCode())
                .append(getEvalue())
                .append(getModelMatchStartPos())
                .append(getAligmentToModel())
                .append(getFamilyEvalue())
                .append(getScopDomainId())
                .append(getScopFamilyId())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
