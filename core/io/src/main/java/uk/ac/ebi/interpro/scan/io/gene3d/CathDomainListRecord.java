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
 * Represents a record in a
 * <a href="http://www.cathdb.info/wiki/data:cathdomainlist">CATH List File (CLF)</a>.
 * For example,
 * <a href="http://release.cathdb.info/v3.3.0/CathDomainList">release 3.3.0</a>.
 * 
 * Format:
 * <pre>
 *   1 	CATH domain name (seven characters)
 *   2 	Class number
 *   3 	Architecture number
 *   4 	Topology number
 *   5 	Homologous superfamily number
 *   6 	S35 sequence cluster number
 *   7 	S60 sequence cluster number
 *   8 	S95 sequence cluster number
 *   9 	S100 sequence cluster number
 *   10 	S100 sequence count number
 *   11 	Domain length
 *   12 	Structure resolution (Angstroms)
 *   (999.000 for NMR structures and 1000.000 for obsolete PDB entries)
 * </pre>
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

    public String getSignatureAccession() {
        return createSignatureAccession(this);
    }    

    public Signature toSignature() {
        return new Signature(getSignatureAccession());
    }

    public static String createSignatureAccession(CathDomainListRecord record)    {
        return createSignatureAccession("G3DSA:", record);
    }

    public static String createSignatureAccession(String prefix, CathDomainListRecord record)    {
        final String SEP = ".";
        StringBuilder builder = new StringBuilder(prefix)
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