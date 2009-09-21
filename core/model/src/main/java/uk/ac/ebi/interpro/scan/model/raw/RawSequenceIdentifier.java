package uk.ac.ebi.interpro.scan.model.raw;

import java.util.Collections;
import java.util.Collection;

/**
 * Stores raw matches associated with a protein sequence identifier.
 * Note: not stored in database, just returned by DAO as a convenience class to use in post-processing.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public final class RawSequenceIdentifier {

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

}
