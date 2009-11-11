package uk.ac.ebi.interpro.scan.parser.matchparser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface Parser {
    public Set<RawSequenceIdentifier> parse(InputStream is) throws IOException;
}
