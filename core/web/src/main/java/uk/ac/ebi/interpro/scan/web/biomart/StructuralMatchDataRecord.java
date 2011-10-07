package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Java object to hold the results of the structural match data REST web service query.
 * Each row becomes a StructuralMatchDataRecord.
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class StructuralMatchDataRecord {

    /*
     * Protein matches TSV column headings:
     *
     * PROTEIN_ACCESSION
     * PROTEIN_ID
     * PROTEIN_LENGTH
     * MD5
     * CRC64
     * database_name
     * domain_id
     * class_id
     * pos_from
     * pos_to
     */

    private final String proteinAc;
    private final String proteinId;
    private final int proteinLength;
    private final String md5;
    private final String crc64;
    private final String databaseName;
    private final String domainId;
    private final String classId;
    private final int posFrom;
    private final int posTo;

    public StructuralMatchDataRecord(String proteinAc, String proteinId, int proteinLength, String md5, String crc64, String databaseName, String domainId, String classId, int posFrom, int posTo) {
        this.proteinAc = proteinAc;
        this.proteinId = proteinId;
        this.proteinLength = proteinLength;
        this.md5 = md5;
        this.crc64 = crc64;
        this.databaseName = databaseName;
        this.domainId = domainId;
        this.classId = classId;
        this.posFrom = posFrom;
        this.posTo = posTo;
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

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getClassId() {
        return classId;
    }

    public int getPosFrom() {
        return posFrom;
    }

    public int getPosTo() {
        return posTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StructuralMatchDataRecord))
            return false;
        final StructuralMatchDataRecord r = (StructuralMatchDataRecord) o;

        return new EqualsBuilder()
                .append(proteinAc, r.proteinAc)
                .append(proteinId, r.proteinId)
                .append(proteinLength, r.proteinLength)
                .append(md5, r.md5)
                .append(crc64, r.crc64)
                .append(databaseName, r.databaseName)
                .append(domainId, r.domainId)
                .append(classId, r.classId)
                .append(posFrom, r.posFrom)
                .append(posTo, r.posTo)
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
                .append(databaseName)
                .append(domainId)
                .append(classId)
                .append(posFrom)
                .append(posTo)
                .toHashCode();
    }

}
