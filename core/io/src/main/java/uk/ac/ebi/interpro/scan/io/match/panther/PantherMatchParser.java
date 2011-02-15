package uk.ac.ebi.interpro.scan.io.match.panther;

import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;

/**
 * Parser for PANTHER output.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class PantherMatchParser
        extends AbstractLineMatchParser<PantherRawMatch>
        implements MatchParser<PantherRawMatch> {

    public PantherMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(signatureLibrary, signatureLibraryRelease);
    }

    // TODO: Implement createMatch()
    @Override protected PantherRawMatch createMatch(String line) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
