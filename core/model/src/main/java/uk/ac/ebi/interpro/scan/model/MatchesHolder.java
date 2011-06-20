package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates protein matches with a signature library release.
 * Note: Not stored in database, just returned by DAO as a convenience class.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@XmlRootElement(name = "protein-matches")
@XmlType(name = "proteinMatchesType")
public final class MatchesHolder implements Serializable {

    private final Set<Protein> proteins = new HashSet<Protein>();

    public MatchesHolder() {
    }

    public void addProtein(Protein protein) {
        proteins.add(protein);
    }

    @XmlElement(name = "protein")
    public Set<Protein> getProteins() {
        return proteins;
    }

    public void addProteins(Collection<Protein> proteins) {
        if (proteins == null) {
            throw new IllegalArgumentException("'Proteins' must not be null");
        }
        this.proteins.addAll(proteins);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MatchesHolder))
            return false;
        final MatchesHolder s = (MatchesHolder) o;
        return new EqualsBuilder()
                .append(proteins, s.proteins)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(71, 53)
                .append(proteins)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
