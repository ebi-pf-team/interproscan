package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates protein matches with a signature library release.
 * Note: Not stored in database, just returned by DAO as a convenience class.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@XmlRootElement(name = "protein-matches")
@XmlType(name = "proteinMatchesType")
public final class ProteinMatchesHolder implements IMatchesHolder, Serializable {

    private final Set<Protein> proteins = new HashSet<Protein>();

    public ProteinMatchesHolder() {
    }

    public void addProtein(Protein protein) {
        proteins.add(protein);
    }

    @XmlElement(name = "protein")
    public Set<Protein> getProteins() {
//        return Collections.unmodifiableSet(proteins);
        return proteins;
    }

    public void addProteins(Collection<Protein> proteins) {
        if (proteins == null) {
            throw new IllegalArgumentException("'Proteins' must not be null");
        }
        this.proteins.addAll(proteins);
    }

//    @XmlElement(name = "protein")
//    public Set<Protein> getProteins() {
//        return Collections.unmodifiableSet(proteins);
//    }
//
//    public void setProteins(Collection<Protein> proteins) {
//        if (proteins == null) {
//            throw new IllegalArgumentException("'Proteins' must not be null");
//        }
//        for (Protein protein : proteins) {
//            addProtein(protein);
//        }
////        this.proteins.addAll(proteins);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProteinMatchesHolder))
            return false;
        final ProteinMatchesHolder s = (ProteinMatchesHolder) o;
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
