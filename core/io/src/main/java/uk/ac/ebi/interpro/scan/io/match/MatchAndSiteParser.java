package uk.ac.ebi.interpro.scan.io.match;

import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;

/**
 * Parser.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public interface MatchAndSiteParser<T extends RawMatch, U extends RawSite> extends Serializable {

    SignatureLibrary getSignatureLibrary();

    String getSignatureLibraryRelease();

    MatchSiteData parse(InputStream is) throws IOException;

}