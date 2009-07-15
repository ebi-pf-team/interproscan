package uk.ac.ebi.interpro.scan.parser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id: Parser.java,v 1.2 2009/02/27 16:50:10 aquinn Exp $
 * @since   1.0
 */
public interface Parser {
    public Set<SequenceIdentifier> parse(InputStream is) throws IOException;
}
