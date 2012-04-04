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
public final class MatchDataRecord extends AbstractDataRecord {

    /*
     * Protein matches TSV column headings:
     *
     * PROTEIN_ACCESSION
     * PROTEIN_ID
     * PROTEIN_LENGTH
     * MD5
     * CRC64
     * METHOD_AC
     * METHOD_NAME
     * METHOD_DATABASE_NAME
     * POS_FROM
     * POS_TO
     * MATCH_SCORE
     * ENTRY_AC
     * ENTRY_SHORT_NAME
     * ENTRY_NAME
     * ENTRY_TYPE
     * TAXONOMY_ID
     * TAXONOMY_SCIENCE_NAME
     * TAXONOMY_FULL_NAME
     * PROTEIN_FRAGMENT
     */

    private final String methodAc;
    private final String methodName;
    private final String methodDatabase;
    private final Double score; // Could be NULL
    private final String entryAc;
    private final String entryShortName;
    private final String entryName;
    private final String entryType;
    private final int taxId;
    private final String taxScienceName;
    private final String taxFullName;

    public MatchDataRecord(String proteinAc, String proteinId, int proteinLength, String md5, String crc64,
                           String methodAc, String methodName, String methodDatabase,
                           int posFrom, int posTo, Double score,
                           String entryAc, String entryShortName, String entryName, String entryType,
                           int taxId, String taxScienceName, String taxFullName, boolean isProteinFragment) {
        super(proteinAc, proteinId, proteinLength, md5, crc64, posFrom, posTo, isProteinFragment);
        this.methodAc = methodAc;
        this.methodName = methodName;
        this.methodDatabase = methodDatabase;
        this.score = score;
        this.entryAc = entryAc;
        this.entryShortName = entryShortName;
        this.entryName = entryName;
        this.entryType = entryType;
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

    public String getMethodDatabase() {
        return methodDatabase;
    }

    public Double getScore() {
        return score;
    }

    public String getEntryAc() {
        return entryAc;
    }

    public String getEntryShortName() {
        return entryShortName;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getEntryType() {
        return entryType;
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
        if (!(o instanceof MatchDataRecord))
            return false;
        final MatchDataRecord r = (MatchDataRecord) o;

        return new EqualsBuilder()
                .append(getProteinAc(), r.getProteinAc())
                .append(getProteinId(), r.getProteinId())
                .append(getProteinLength(), r.getProteinLength())
                .append(getMd5(), r.getMd5())
                .append(getCrc64(), r.getCrc64())
                .append(methodAc, r.methodAc)
                .append(methodName, r.methodName)
                .append(methodDatabase, r.methodDatabase)
                .append(getPosFrom(), r.getPosFrom())
                .append(getPosTo(), r.getPosTo())
                .append(score, r.score)
                .append(entryAc, r.entryAc)
                .append(entryShortName, r.entryShortName)
                .append(entryName, r.entryName)
                .append(entryType, r.entryType)
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
                .append(getProteinLength())
                .append(getMd5())
                .append(getCrc64())
                .append(methodAc)
                .append(methodName)
                .append(methodDatabase)
                .append(getPosFrom())
                .append(getPosTo())
                .append(score)
                .append(entryAc)
                .append(entryShortName)
                .append(entryName)
                .append(entryType)
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
