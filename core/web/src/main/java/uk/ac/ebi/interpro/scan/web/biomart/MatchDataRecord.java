package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Java object to hold the results of the match data REST web service query.
 * Each row becomes a MatchDataRecord.
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public final class MatchDataRecord {

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
     */

    private final String proteinAc;
    private final String proteinId;
    private final int proteinLength;
    private final String md5;
    private final String crc64;
    private final String methodAc;
    private final String methodName;
    private final String methodType;
    private final int posFrom;
    private final int posTo;
    private final Double score; // Could be NULL
    private final String entryAc;
    private final String entryShortName;
    private final String entryName;
    private final String entryType;
    private final int taxId;
    private final String taxScienceName;
    private final String taxFullName;

    public MatchDataRecord(String proteinAc, String proteinId, int proteinLength, String md5, String crc64,
                              String methodAc, String methodName, String methodType,
                              int posFrom, int posTo, Double score,
                              String entryAc, String entryShortName, String entryName, String entryType,
                              int taxId, String taxScienceName, String taxFullName){
        this.proteinAc = proteinAc;
        this.proteinId = proteinId;
        this.proteinLength = proteinLength;
        this.md5 = md5;
        this.crc64 = crc64;
        this.methodAc = methodAc;
        this.methodName = methodName;
        this.methodType = methodType;
        this.posFrom = posFrom;
        this.posTo = posTo;
        this.score = score;
        this.entryAc = entryAc;
        this.entryShortName = entryShortName;
        this.entryName = entryName;
        this.entryType = entryType;
        this.taxId = taxId;
        this.taxScienceName = taxScienceName;
        this.taxFullName = taxFullName;
    }

    public String getProteinAc() {
        return proteinAc;
    }

    public String getProteinId() {
        return proteinId;
    }

    public int getProteinLength() {
        return proteinLength;
    }

    public String getMd5() {
        return md5;
    }

    public String getCrc64() {
        return crc64;
    }

    public String getMethodAc() {
        return methodAc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodType() {
        return methodType;
    }

    public int getPosFrom() {
        return posFrom;
    }

    public int getPosTo() {
        return posTo;
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
                .append(proteinAc, r.proteinAc)
                .append(proteinId, r.proteinId)
                .append(proteinLength, r.proteinLength)
                .append(md5, r.md5)
                .append(crc64, r.crc64)
                .append(methodAc, r.methodAc)
                .append(methodName, r.methodName)
                .append(methodType, r.methodType)
                .append(posFrom, r.posFrom)
                .append(posTo, r.posTo)
                .append(score, r.score)
                .append(entryAc, r.entryAc)
                .append(entryShortName, r.entryShortName)
                .append(entryName, r.entryName)
                .append(entryType, r.entryType)
                .append(taxId, r.taxId)
                .append(taxScienceName, r.taxScienceName)
                .append(taxFullName, r.taxFullName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 15)
                .append(proteinAc)
                .append(proteinId)
                .append(proteinLength)
                .append(md5)
                .append(crc64)
                .append(methodAc)
                .append(methodName)
                .append(methodType)
                .append(posFrom)
                .append(posTo)
                .append(score)
                .append(entryAc)
                .append(entryShortName)
                .append(entryName)
                .append(entryType)
                .append(taxId)
                .append(taxScienceName)
                .append(taxFullName)
                .toHashCode();
    }


    @Override
    public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }
}
