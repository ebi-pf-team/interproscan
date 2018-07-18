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
 * Stores raw matches associated with a protein sequence identifier.
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
public final class RawProtein<T extends RawMatch> implements Serializable {

    private final String proteinIdentifier;
    private final Collection<T> matches = new HashSet<T>();
    private Long proteinDatabaseId;

    private RawProtein() {
        this.proteinIdentifier = null;
    }

    public RawProtein(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public String getProteinIdentifier() {
        return proteinIdentifier;
    }

    public void addMatch(T match) {
        if (proteinDatabaseId == null) {
            proteinDatabaseId = match.getNumericSequenceId();
        }
        matches.add(match);
    }

    public void setMatches(Set<T> matches) {
        //this.matches.clear();
        this.addAllMatches(matches);
    }

    public Collection<T> getMatches() {
//        return Collections.unmodifiableCollection(matches);
        return matches;
    }

    public Long getProteinDatabaseId() {
        return proteinDatabaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawProtein))
            return false;
        final RawProtein s = (RawProtein) o;
        return new EqualsBuilder()
                .append(proteinIdentifier, s.proteinIdentifier)
                .append(matches, s.matches)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 53)
                .append(proteinIdentifier)
                .append(matches)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // Convenience method for adding a Set of matches in one go.

    public void addAllMatches(Set<T> rawMatches) {
        if (rawMatches != null) {
            for (T match : rawMatches) {
                addMatch(match);
            }
        }
    }
}
