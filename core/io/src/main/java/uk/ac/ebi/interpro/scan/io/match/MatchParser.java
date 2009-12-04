package uk.ac.ebi.interpro.scan.io.match;

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.io.ParseException;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface MatchParser extends Serializable {
    public <T extends RawMatch> Set<RawProtein<T>> parse(InputStream is) throws IOException, ParseException;
}
