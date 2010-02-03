package uk.ac.ebi.interpro.scan.io.gene3d;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Model;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO: Add class description
 *
 * http://www.cathdb.info/wiki/data:cathdomainlist
 *
 * http://release.cathdb.info/v3.3.0/CathDomainList
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class CathDomainListRecord {

    // We're only interested in the first five fields
    private final String domainName;
    private final int classNumber;
    private final int architectureNumber;
    private final int topologyNumber;
    private final int homologousSuperfamilyNumber;

    public CathDomainListRecord(String domainName, int classNumber, int architectureNumber,
                                int topologyNumber, int homologousSuperfamilyNumber) {
        this.domainName = domainName;
        this.classNumber = classNumber;
        this.architectureNumber = architectureNumber;
        this.topologyNumber = topologyNumber;
        this.homologousSuperfamilyNumber = homologousSuperfamilyNumber;
    }

    public String getDomainName() {
        return domainName;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public int getArchitectureNumber() {
        return architectureNumber;
    }

    public int getTopologyNumber() {
        return topologyNumber;
    }

    public int getHomologousSuperfamilyNumber() {
        return homologousSuperfamilyNumber;
    }

    public Signature toSignature() {
        return new Signature(createSignatureAccession(this));
    }

    public static String createSignatureAccession(CathDomainListRecord record)    {
        final String SEP = ".";
        StringBuilder builder = new StringBuilder("G3DSA:")
                .append(record.getClassNumber()).append(SEP)
                .append(record.getArchitectureNumber()).append(SEP)
                .append(record.getTopologyNumber()).append(SEP)
                .append(record.getHomologousSuperfamilyNumber());
        return builder.toString();
    }

    public static Collection<Signature> createSignatures(Collection<CathDomainListRecord> records)  {
        Map<String, Signature> map = new HashMap<String, Signature>();
        for (CathDomainListRecord record : records) {
            String ac = createSignatureAccession(record);
            Signature signature;
            if (map.containsKey(ac))    {
                signature = map.get(ac);
            }
            else    {
                signature = new Signature(ac);
                map.put(ac, signature);
            }
            signature.addModel(new Model(record.getDomainName()));
        }
        return map.values();
    }
    
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CathDomainListRecord))
            return false;
        final CathDomainListRecord r = (CathDomainListRecord) o;
        return new EqualsBuilder()
                .append(domainName, r.domainName)
                .append(classNumber, r.classNumber)
                .append(architectureNumber, r.architectureNumber)
                .append(topologyNumber, r.topologyNumber)
                .append(homologousSuperfamilyNumber, r.homologousSuperfamilyNumber)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(27, 13)
                .append(domainName)
                .append(classNumber)
                .append(architectureNumber)
                .append(topologyNumber)
                .append(homologousSuperfamilyNumber)
                .toHashCode();
    }


    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}