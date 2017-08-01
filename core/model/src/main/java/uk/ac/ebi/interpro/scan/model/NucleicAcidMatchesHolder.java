package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Root object of the XML output for nucleic aicd track back.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@XmlRootElement(name = "nucleotide-sequence-matches")
@XmlType(name = "nucleicAcidMatchesType")
public final class NucleicAcidMatchesHolder implements IMatchesHolder, Serializable {

    private String interProScanVersion = "Unknown";
    private final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();

    public NucleicAcidMatchesHolder() {}

    public NucleicAcidMatchesHolder(String interProScanVersion) {
        this.interProScanVersion = interProScanVersion;
    }

    @XmlAttribute(name = "interproscan-version")
    public String getInterProScanVersion() {
        return interProScanVersion;
    }

    public void addProteins(Collection<Protein> proteins) {
        for (Protein protein : proteins) {
            addProtein(protein);
        }
    }

    public void addProtein(Protein protein) {
        if (protein == null) {
            throw new IllegalArgumentException("'Protein' must not be null");
        }
        for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
            NucleotideSequence seq = orf.getNucleotideSequence();
            if (seq != null) {
                nucleotideSequences.add(seq);
            }
        }
    }

    @XmlElement(name = "nucleotide-sequence")
    public Set<NucleotideSequence> getNucleotideSequences() {
        return nucleotideSequences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NucleicAcidMatchesHolder))
            return false;
        final NucleicAcidMatchesHolder s = (NucleicAcidMatchesHolder) o;
        return new EqualsBuilder()
                .append(interProScanVersion, s.interProScanVersion)
                .append(nucleotideSequences, s.nucleotideSequences)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(71, 53)
                .append(interProScanVersion)
                .append(nucleotideSequences)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
