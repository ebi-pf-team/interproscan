package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Stores raw sites associated with a protein sequence identifier.
 * </p>
 * <p>
 * We cannot associate raw matches with a {@link uk.ac.ebi.interpro.scan.model.Protein} when
 * parsing output files because we do not have the protein sequence and cannot therefore create a Protein
 * object. We must therefore use this class when parsing because all we have is a protein identifier.
 * </p>
 * <p>
 * Note: Not stored in database, just returned by DAO as a convenience class.
 * </p>
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
public final class RawProteinSite<T extends RawSite> implements Serializable {

    private final String proteinIdentifier;
    private final Collection<T> sites = new HashSet<T>();
    private Long proteinDatabaseId;

    private RawProteinSite() {
        this.proteinIdentifier = null;
    }

    public RawProteinSite(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public String getProteinIdentifier() {
        return proteinIdentifier;
    }

    public void addSite(T site) {
        if (proteinDatabaseId == null) {
            proteinDatabaseId = site.getNumericSequenceId();
        }
        sites.add(site);
    }

    public Collection<T> getSites() {
        return sites;
    }

    public Long getProteinDatabaseId() {
        return proteinDatabaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawProteinSite))
            return false;
        final RawProteinSite s = (RawProteinSite) o;
        return new EqualsBuilder()
                .append(proteinIdentifier, s.proteinIdentifier)
                .append(sites, s.sites)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 53)
                .append(proteinIdentifier)
                .append(sites)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // Convenience method for adding a Set of matches in one go.

    public void addAllSites(Set<T> rawSites) {
        if (rawSites != null) {
            for (T site : rawSites) {
                addSite(site);
            }
        }
    }
}
