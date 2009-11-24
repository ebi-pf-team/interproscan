package uk.ac.ebi.interpro.scan.parser.matchparser;

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import uk.ac.ebi.interpro.scan.parser.ParseException;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface Parser extends Serializable {
    public Set<RawSequenceIdentifier> parse(InputStream is) throws IOException, ParseException;
}
