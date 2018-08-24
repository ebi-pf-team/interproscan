package uk.ac.ebi.interpro.scan.web.io;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Java object to hold the results of the match data REST web service query.
 * Each row becomes a MatchDataRecord.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public final class FeatureMatchDataRecord extends AbstractDataRecord {

    /*
     * Protein matches TSV column headings:
     *
     * PROTEIN_ACCESSION
     * PROTEIN_ID
     * PROTEIN_LENGTH
     * CRC64
     * METHOD_AC
     * METHOD_NAME
     * METHOD_DATABASE_NAME
     * POS_FROM
     * POS_TO
     * SEQ_FEATURE
     * TAXONOMY_ID
     * TAXONOMY_SCIENCE_NAME
     * TAXONOMY_FULL_NAME
     * PROTEIN_FRAGMENT
     */

    private final String methodAc;
    private final String methodName;
    private final String seqFeature; // Could be NULL
    private final String methodDatabase;
    private final int taxId;
    private final String taxScienceName;
    private final String taxFullName;

    public FeatureMatchDataRecord(String proteinAc, String proteinId, String proteinDescription, int proteinLength,
                                  String crc64, String methodAc, String methodName, String methodDatabase,
                                  int posFrom, int posTo, String seqFeature,
                                  int taxId, String taxScienceName, String taxFullName, boolean isProteinFragment) {
        super(proteinAc, proteinId, proteinDescription, proteinLength, crc64, posFrom, posTo, isProteinFragment);
        this.methodAc = methodAc;
        this.methodName = methodName;
        this.seqFeature = seqFeature;
        this.methodDatabase = methodDatabase;
        this.taxId = taxId;
        this.taxScienceName = taxScienceName;
        this.taxFullName = taxFullName;
    }

    public String getMethodAc() {
        return methodAc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getSeqFeature() {
        return seqFeature;
    }

    public String getMethodDatabase() {
        return methodDatabase;
    }

    public int getTaxId() {
        return taxId;
    }

    public String getTaxScienceName() {
        return taxScienceName;
    }

    public String getTaxFullName() {
        return taxFullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FeatureMatchDataRecord))
            return false;
        final FeatureMatchDataRecord r = (FeatureMatchDataRecord) o;

        return new EqualsBuilder()
                .append(getProteinAc(), r.getProteinAc())
                .append(getProteinId(), r.getProteinId())
                .append(getProteinDescription(), r.getProteinDescription())
                .append(getProteinLength(), r.getProteinLength())
                .append(getCrc64(), r.getCrc64())
                .append(methodAc, r.methodAc)
                .append(methodName, r.getMethodName())
                .append(seqFeature, r.seqFeature)
                .append(methodDatabase, r.methodDatabase)
                .append(getPosFrom(), r.getPosFrom())
                .append(getPosTo(), r.getPosTo())
                .append(taxId, r.taxId)
                .append(taxScienceName, r.taxScienceName)
                .append(taxFullName, r.taxFullName)
                .append(isProteinFragment(), r.isProteinFragment())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 15)
                .append(getProteinAc())
                .append(getProteinId())
                .append(getProteinDescription())
                .append(getProteinLength())
                .append(getCrc64())
                .append(methodAc)
                .append(methodName)
                .append(seqFeature)
                .append(methodDatabase)
                .append(getPosFrom())
                .append(getPosTo())
                .append(taxId)
                .append(taxScienceName)
                .append(taxFullName)
                .append(isProteinFragment())
                .toHashCode();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
