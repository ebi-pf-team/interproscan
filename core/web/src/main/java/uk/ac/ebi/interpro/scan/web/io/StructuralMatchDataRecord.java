package uk.ac.ebi.interpro.scan.web.io;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Java object to hold the results of the structural match data REST web service query.
 * Each row becomes a StructuralMatchDataRecord.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class StructuralMatchDataRecord extends AbstractDataRecord {

    /*
     * Protein matches TSV column headings:
     *
     * PROTEIN_ACCESSION
     * PROTEIN_ID
     * PROTEIN_LENGTH
     * CRC64
     * database_name
     * domain_id
     * class_id
     * pos_from
     * pos_to
     * PROTEIN_FRAGMENT
     */

    private final String databaseName;
    private final String domainId;
    private final String classId;

    public StructuralMatchDataRecord(String proteinAc, String proteinId, String proteinDescription, int proteinLength,
                                     String crc64, String databaseName, String domainId, String classId, int posFrom,
                                     int posTo, boolean isProteinFragment) {
        super(proteinAc, proteinId, proteinDescription, proteinLength, crc64, posFrom, posTo, isProteinFragment);
        this.databaseName = databaseName;
        this.domainId = domainId;
        this.classId = classId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StructuralMatchDataRecord))
            return false;
        final StructuralMatchDataRecord r = (StructuralMatchDataRecord) o;

        return new EqualsBuilder()
                .append(getProteinAc(), r.getProteinAc())
                .append(getProteinId(), r.getProteinId())
                .append(getProteinDescription(), r.getProteinDescription())
                .append(getProteinLength(), r.getProteinLength())
                .append(getCrc64(), r.getCrc64())
                .append(databaseName, r.databaseName)
                .append(domainId, r.domainId)
                .append(classId, r.classId)
                .append(getPosFrom(), r.getPosFrom())
                .append(getPosTo(), r.getPosTo())
                .append(isProteinFragment(), r.isProteinFragment())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 15)
                .append(getProteinAc())
                .append(getProteinAc())
                .append(getProteinDescription())
                .append(getProteinLength())
                .append(getCrc64())
                .append(databaseName)
                .append(domainId)
                .append(classId)
                .append(getPosFrom())
                .append(getPosTo())
                .append(isProteinFragment())
                .toHashCode();
    }

}
