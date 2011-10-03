package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Java object to hold the results of the BioMart query, each query row becomes a BioMartQueryRecord.
 *
 * @author  Matthew Fraser
 * @version $Id$

 */
public final class BioMartQueryRecord {

    /*
     * BioMart query XML of interest:
     *
     * <Attribute name = 'protein_accession' />
     * <Attribute name = 'protein_name' />
     * <Attribute name = 'md5' />
     * <Attribute name = 'method_id' />
     * <Attribute name = 'method_name' />
     * <Attribute name = 'method_database_name' />
     * <Attribute name = 'pos_from' />
     * <Attribute name = 'pos_to' />
     * <Attribute name = 'match_score' />
     * <Attribute name = 'entry_ac' />
     * <Attribute name = 'entry_short_name' />
     * <Attribute name = 'entry_name' />
     * <Attribute name = 'entry_type' />
     */

    private final String proteinAc;
    private final String proteinName;
    private final String methodAc;
    private final String methodName;
    private final String methodType;
    private final int posFrom;
    private final int posTo;
    private final String entryAc;
    private final String entryName;
    private final String entryType;

    public BioMartQueryRecord(String proteinAc, String proteinName,
                              String methodAc, String methodName, String methodType,
                              int posFrom, int posTo,
                              String entryAc, String entryName, String entryType){
        this.proteinAc = proteinAc;
        this.proteinName = proteinName;
        this.methodAc = methodAc;
        this.methodName = methodName;
        this.methodType = methodType;
        this.posFrom = posFrom;
        this.posTo = posTo;
        this.entryAc = entryAc;
        this.entryName = entryName;
        this.entryType = entryType;
    };

    public String getProteinAc() {
        return proteinAc;
    }

    public String getProteinName() {
        return proteinName;
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

    public String getEntryAc() {
        return entryAc;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BioMartQueryRecord))
            return false;
        final BioMartQueryRecord r = (BioMartQueryRecord) o;

        return new EqualsBuilder()
                .append(proteinAc, r.proteinAc)
                .append(proteinName, r.proteinName)
                .append(methodAc, r.methodAc)
                .append(methodName, r.methodName)
                .append(methodType, r.methodType)
                .append(posFrom, r.posFrom)
                .append(posTo, r.posTo)
                .append(entryAc, r.entryAc)
                .append(entryName, r.entryName)
                .append(entryType, r.entryType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 15)
                .append(proteinAc)
                .append(proteinName)
                .append(methodAc)
                .append(methodName)
                .append(methodType)
                .append(posFrom)
                .append(posTo)
                .append(entryAc)
                .append(entryName)
                .append(entryType)
                .toHashCode();
    }


    @Override
    public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }
}
