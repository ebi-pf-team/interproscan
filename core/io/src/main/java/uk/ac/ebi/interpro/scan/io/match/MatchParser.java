package uk.ac.ebi.interpro.scan.io.match;

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface MatchParser<T extends RawMatch> extends Serializable {

    SignatureLibrary getSignatureLibrary();

    String getSignatureLibraryRelease();
    
    Set<RawProtein<T>> parse(InputStream is) throws IOException;

}