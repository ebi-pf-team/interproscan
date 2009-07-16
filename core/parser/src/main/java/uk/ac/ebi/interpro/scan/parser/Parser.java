package uk.ac.ebi.interpro.scan.parser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface Parser {
    public Set<SequenceIdentifier> parse(InputStream is) throws IOException;
}
