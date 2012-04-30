package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import java.util.UUID;

/**
 * SuperFamily raw match
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class SuperFamilyHmmer3RawMatch extends RawMatch {

    private double evalue;

    private int modelMatchStartPos;

    private String aligmentToModel;

    private double familyEvalue;

    private int scopDomainId;

    private int scopFamilyId;

    private UUID splitGroup;

    protected SuperFamilyHmmer3RawMatch() {
    }

    public SuperFamilyHmmer3RawMatch(String sequenceIdentifier, String model,
                                     String signatureLibraryRelease,
                                     int locationStart, int locationEnd,
                                     double evalue, int modelMatchStartPos,
                                     String alignmentToModel, double familyEvalue,
                                     int scopDomainId, int scopFamilyId,
                                     UUID splitGroup) {
        super(sequenceIdentifier, model, SignatureLibrary.SUPERFAMILY, signatureLibraryRelease, locationStart, locationEnd);
        this.evalue = evalue;
        this.modelMatchStartPos = modelMatchStartPos;
        this.aligmentToModel = alignmentToModel;
        this.familyEvalue = familyEvalue;
        this.scopDomainId = scopDomainId;
        this.scopFamilyId = scopFamilyId;
        this.splitGroup = splitGroup;
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

    public UUID getSplitGroup() {
        return splitGroup;
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
