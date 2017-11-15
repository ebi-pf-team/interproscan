package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <a href="http://gene3d.biochem.ucl.ac.uk/Gene3D/">Gene3D</a> raw match.
 * <p/>
 * TODO: PJ: Consider if the functionality in this class should be in a second abstract
 * TODO: class for any HMMER3 member database that needs to store the alignment.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = Gene3dHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "G3D_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "G3D_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "G3D_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "G3D_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "G3D_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class Gene3dHmmer3RawMatch extends Hmmer3RawMatch {

    @Transient
    public static final String TABLE_NAME = "GENE3D_HMMER3_RAW_MATCH";

    @Column(nullable = false, length = 4000)
    private String cathFamilyId;

    @Column(nullable = false, length = 4000)
    private String hitModelName;

//    @Column(nullable = false, length = 4000)
//    private String alignedRegions;

    @Column(nullable = false, length = 4000)
    private String cigarAlignment;

    @Column(nullable = false, length = 4000)
    private String regionComment;

    protected Gene3dHmmer3RawMatch() {
    }

    public Gene3dHmmer3RawMatch(String sequenceIdentifier, String model,
                                String cathFamilyId, String hitModelName,
                                String signatureLibraryRelease,
                                int locationStart, int locationEnd,
                                double evalue, double score,
                                int hmmStart, int hmmEnd, String hmmBounds,
                                double locationScore,
                                int envelopeStart, int envelopeEnd,
                                double expectedAccuracy, double fullSequenceBias,
                                double domainCeValue, double domainIeValue, double domainBias,
                                String alignedRegions, String regionComment) {
        super(sequenceIdentifier, model, SignatureLibrary.GENE3D, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy,
                fullSequenceBias, domainCeValue, domainIeValue, domainBias);
        setCathFamilyId(cathFamilyId);
        setHitModelName(hitModelName);
        setAlignedRegions(alignedRegions);
        setRegionComment(regionComment);
    }

    public String getAlignedRegions() {
        return cigarAlignment;
//        return alignedRegions;
    }

    public void setAlignedRegions(String alignedRegions) {
        this.cigarAlignment = alignedRegions;
//        this.alignedRegions = alignedRegions;
    }

    public String getCathFamilyId() {
        return this.cathFamilyId;
    }

    public void setCathFamilyId(String cathFamilyId) {
        this.cathFamilyId = cathFamilyId;
    }

    public String getHitModelName() {
        return hitModelName;
    }

    public void setHitModelName(String hitModelName) {
        this.hitModelName = hitModelName;
    }

    public String getRegionComment() {
        return regionComment;
    }

    public void setRegionComment(String regionComment) {
        this.regionComment = regionComment;
    }

    //    private void setCigarAlignment(String cigarAlignment) {
//        this.cigarAlignment = cigarAlignment;
//    }

//    public String getCigarAlignment() {
//        return cigarAlignment;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Gene3dHmmer3RawMatch))
            return false;
        final Gene3dHmmer3RawMatch m = (Gene3dHmmer3RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getAlignedRegions(), m.getAlignedRegions())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 61)
                .appendSuper(super.hashCode())
                .append(getAlignedRegions())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
