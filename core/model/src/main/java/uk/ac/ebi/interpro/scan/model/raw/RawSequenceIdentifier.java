package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Collection;
import java.io.Serializable;

/**
 * Stores raw matches associated with a protein sequence identifier.
 *
 * We cannot associate raw matches with a {@link uk.ac.ebi.interpro.scan.model.Protein} when
 * parsing output files because we do not have the protein sequence and cannot therefore create a Protein
 * object. We must therefore use this class when parsing because all we have is a protein identifier. 
 *
 * Note: Not stored in database, just returned by DAO as a convenience class.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class RawSequenceIdentifier implements Serializable {

    private final String sequenceIdentifier;
    private final Collection<? extends RawMatch> matches;

    private RawSequenceIdentifier() {
        this.sequenceIdentifier = null;
        this.matches = null;
    }

    public RawSequenceIdentifier(String sequenceIdentifier, Collection<? extends RawMatch> matches) {
        this.sequenceIdentifier = sequenceIdentifier;
        this.matches = matches;
    }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public Collection<? extends RawMatch> getMatches() {
        return Collections.unmodifiableCollection(matches);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawSequenceIdentifier))
            return false;
        final RawSequenceIdentifier s = (RawSequenceIdentifier) o;
        return new EqualsBuilder()
                .append(sequenceIdentifier, s.sequenceIdentifier)
                .append(matches, s.matches)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(21, 53)
                .append(sequenceIdentifier)
                .append(matches)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
