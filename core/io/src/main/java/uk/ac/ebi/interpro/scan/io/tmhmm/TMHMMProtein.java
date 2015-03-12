package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMProtein implements Serializable {

    private final String proteinIdentifier;
    private final Collection<TMHMMMatch> matches = new HashSet<TMHMMMatch>();

    private TMHMMProtein() {
        this.proteinIdentifier = null;
    }

    public TMHMMProtein(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public String getProteinIdentifier() {
        return proteinIdentifier;
    }

    public void addMatch(TMHMMMatch match) {
        matches.add(match);
    }

    public Collection<TMHMMMatch> getMatches() {
        return Collections.unmodifiableCollection(matches);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TMHMMProtein))
            return false;
        final TMHMMProtein s = (TMHMMProtein) o;
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

    public void addAllMatches(Set<TMHMMMatch> matches) {
        if (matches != null) {
            for (TMHMMMatch match : matches) {
                addMatch(match);
            }
        }
    }
}